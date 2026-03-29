package iut.dam.powerhome;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RequetesFragment extends Fragment {


    private View layout;
    private TextView tvMonth, btnPrev2Weeks, btnNext2Weeks;
    private RecyclerView rvCalendarDays;
    private LinearLayout requestCard, layoutApplianceIcons, layoutReservations;
    private TextView tvSelectedDate, tvSlotMorning, tvSlotAfternoon, tvSlotEvening;
    private TextView tvBonusEcoCoin, tvSoldeInitial, tvTotalBalance, tvWarningMessage, btnConfirm;

    private User currentUser;
    private LocalDate currentWindowStartDate;
    private CalendarDay selectedDay;
    private CalendarDayAdapter calendarDayAdapter;
    private String selectedSlotColor = null; //nulle si aucun slot selected
    private Timeslot selectedTimeslot = null;
    private Appliance selectedAppliance = null;

    private static final String BASE_URL = "http://10.0.2.2/www/";

    public RequetesFragment() {}

    //point entry du fragment
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.requetes_fragment, container, false);
        initViews();
        loadSessionUser(); //charger depuis memoire locale logique
        initCalendar();
        renderAppliances(currentUser != null && currentUser.habitat != null
                ? currentUser.habitat.getAppliances() : null);
        hideCreneauSection();//cache creneau au demarrage
        loadReservations();
        return layout;
    }

    //link entre chaque variable et son xml avc le findview
    private void initViews() {
        tvMonth              = layout.findViewById(R.id.tv_month);
        btnPrev2Weeks        = layout.findViewById(R.id.btn_prev_2weeks);
        btnNext2Weeks        = layout.findViewById(R.id.btn_next_2weeks);
        rvCalendarDays       = layout.findViewById(R.id.rv_calendar_days);
        requestCard          = layout.findViewById(R.id.request_card);
        tvSelectedDate       = layout.findViewById(R.id.tv_selected_date);
        tvSlotMorning        = layout.findViewById(R.id.tv_slot_morning);
        tvSlotAfternoon      = layout.findViewById(R.id.tv_slot_afternoon);
        tvSlotEvening        = layout.findViewById(R.id.tv_slot_evening);
        tvBonusEcoCoin       = layout.findViewById(R.id.tv_bonus_ecocoin);
        tvSoldeInitial       = layout.findViewById(R.id.tv_solde_initial);
        tvTotalBalance       = layout.findViewById(R.id.tv_total_balance);
        tvWarningMessage     = layout.findViewById(R.id.tv_warning_message);
        btnConfirm           = layout.findViewById(R.id.btn_confirm);
        layoutApplianceIcons = layout.findViewById(R.id.layout_appliance_icons);
        layoutReservations   = layout.findViewById(R.id.layout_reservations);
    }

    //lit le json saved in sharedpreferences (la cle user_json du fichier user_session
    //et le convertit en obj user (avc le .getfromjson() ->json du loginactvity
    //si null -toast et on arrete
    private void loadSessionUser() {
        if (getContext() == null) return;
        String json = getContext().getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("user_json", null);
        if (json == null) { toast(getString(R.string.error_no_user_connected)); return; }
        currentUser = User.getFromJson(json);
    }

    //Partie du calendrier :
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initCalendar() {
        currentWindowStartDate = LocalDate.now();
        //memorise chaque j clique, renitialise les sections creneau/appliances
        //affiche les creneaux et maj des affichages
        calendarDayAdapter = new CalendarDayAdapter(requireContext(), day -> {
            selectedDay = day;
            selectedTimeslot = null;
            selectedSlotColor = null;
            showCreneauSection();
            tvSelectedDate.setText(formatDate(day.getDate()));
            updateSlotsFromDay(day);
            updateEcoCoinPreview();
            updateSoldeDisplays();
        });
        //avc ce GridLayoutManager, les items disposes en 7 colonnes (ducoup 2 lignes de 7j)
        //currentWindowStartDate va brancher les btn prev et next (< et >), qui vont activement modifier
        //la current window de + ou- 14j puis relance le load pour reload
        rvCalendarDays.setLayoutManager(new GridLayoutManager(getContext(), 7));
        rvCalendarDays.setAdapter(calendarDayAdapter);
        btnPrev2Weeks.setOnClickListener(v -> { currentWindowStartDate = currentWindowStartDate.minusDays(14); loadCalendarStatus(); });
        btnNext2Weeks.setOnClickListener(v -> { currentWindowStartDate = currentWindowStartDate.plusDays(14);  loadCalendarStatus(); });
        loadCalendarStatus();
        // le load rappelle -t-on, va appel HTTP GET vers getcalendarstatut.php, avc token et date de deb
        //reponse: json avc nom du mois et 14 obj , puis on va donner les j a ladapter pour dessiner la grille
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadCalendarStatus() {
        if (currentUser == null || currentUser.token == null) return;
        String url = BASE_URL + "getCalendarStatus.php?token=" + currentUser.token
                + "&start_date=" + currentWindowStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        Ion.with(getContext()).load(url).asString().setCallback((e, r) -> {
            if (e != null) { toast(getString(R.string.error_calendar)); return; }
            CalendarStatusResponse cr = CalendarStatusResponse.getFromJson(r);
            if (cr == null || cr.getDays() == null) return;
            tvMonth.setText(cr.getMonth_label());
            calendarDayAdapter.setDays(cr.getDays());
        });
    }

    //Partie creneaux (slot):
    //updateslot, appellee juste apres un cloc sur un jour, et extraie les 3 creneaux
    //avec slot order comme index s[], false car pas encore selectionne
    //branche les setonClick, sur chaque bouton avc le bon timeslot
    private void updateSlotsFromDay(CalendarDay day) {
        Timeslot[] s = extractSlots(day);
        applySlot(tvSlotMorning,   s[0], false);
        applySlot(tvSlotAfternoon, s[1], false);
        applySlot(tvSlotEvening,   s[2], false);
        tvSlotMorning.setOnClickListener(v   -> selectSlot(s[0], tvSlotMorning));
        tvSlotAfternoon.setOnClickListener(v -> selectSlot(s[1], tvSlotAfternoon));
        tvSlotEvening.setOnClickListener(v   -> selectSlot(s[2], tvSlotEvening));
    }


    //transforme la liste de timeslot en un tableau 3 case
    private Timeslot[] extractSlots(CalendarDay day) {
        Timeslot[] s = new Timeslot[3];
        if (day != null && day.getSlots() != null)
            for (Timeslot t : day.getSlots())
                if (t.getSlot_order() >= 1 && t.getSlot_order() <= 3) s[t.getSlot_order() - 1] = t;
        return s;
    }

    //applique le style visuel a un bouton creneau selon son etat, prend en couleur la couleur d'affluence
    private void applySlot(TextView tv, Timeslot t, boolean selected) {
        String c = t != null ? t.getColor() : null;
        if (c == null || c.isEmpty()) {
            tv.setTextColor(color(R.color.gray));
            tv.setBackgroundTintList(ColorStateList.valueOf(color(R.color.off_white)));
            tv.setAlpha(1f);
            return;
        }
        int res = colorRes(c);
        if (selected) {
            tv.setTextColor(color(R.color.white));
            tv.setBackgroundTintList(ColorStateList.valueOf(color(res)));
            tv.setAlpha(1f);
        } else {
            tv.setTextColor(color(res));
            tv.setBackgroundTintList(ColorStateList.valueOf(color(R.color.off_white)));
            tv.setAlpha(0.85f);
        }
    }

    //selectSlot : memorise le creneau dans selectedTimeslot et sa color dans selectedSlotColor
    //redessine les 3 boutons, maj direct des affichage ecocoin et solde
    private void selectSlot(Timeslot t, TextView tapped) {
        selectedTimeslot = t;
        selectedSlotColor = (t != null && t.getColor() != null) ? t.getColor() : "green";
        Timeslot[] s = extractSlots(selectedDay);
        applySlot(tvSlotMorning,   s[0], tapped == tvSlotMorning);
        applySlot(tvSlotAfternoon, s[1], tapped == tvSlotAfternoon);
        applySlot(tvSlotEvening,   s[2], tapped == tvSlotEvening);
        updateEcoCoinPreview();
        updateSoldeDisplays();
    }

    //Partie des appliances: genere les icones en code car liste dynamique,

    //pour chaque appliance, cree un linerailayout avc une imgview et un text view (la reference)
    //le clic appelle toggleAppliance()
    private void renderAppliances(List<Appliance> appliances) {
        if (layoutApplianceIcons == null) return;
        layoutApplianceIcons.removeAllViews();
        selectedAppliance = null;
        if (appliances == null) return;
        int sizePx = (int) (64 * getResources().getDisplayMetrics().density);
        for (Appliance a : appliances) {
            LinearLayout cell = new LinearLayout(getContext());
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cp.setMargins(0, 0, 28, 0);
            cell.setLayoutParams(cp);

            ImageView iv = new ImageView(getContext());
            iv.setImageResource(a.getD());
            iv.setColorFilter(color(R.color.gray));
            iv.setAlpha(0.65f);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setLayoutParams(new LinearLayout.LayoutParams(sizePx, sizePx));
            iv.setPadding(8, 8, 8, 8);

            TextView tvRef = new TextView(getContext());
            tvRef.setText(a.getReference());
            tvRef.setTextSize(12f);
            tvRef.setTextColor(color(R.color.gray));
            tvRef.setGravity(Gravity.CENTER);

            cell.addView(iv);
            cell.addView(tvRef);
            cell.setOnClickListener(v -> toggleAppliance(a, iv, tvRef));
            layoutApplianceIcons.addView(cell);
        }
    }

    //si appliance deja selected -> deselect, sinon reset de tout les appliances via
    //resetAllApplianceViews() et select du nv (selection exclusive 1 a la fois)
    private void toggleAppliance(Appliance a, ImageView iv, TextView tvRef) {
        if (selectedAppliance != null && selectedAppliance.getId() == a.getId()) {
            selectedAppliance = null;
            setApplianceStyle(iv, tvRef, false);
        } else {
            resetAllApplianceViews();
            selectedAppliance = a;
            setApplianceStyle(iv, tvRef, true);
        }
    }

    //applique style actif (vert, opacity 100) icone + label, factorise
    private void setApplianceStyle(ImageView iv, TextView tvRef, boolean active) {
        iv.setColorFilter(color(active ? R.color.dark_green : R.color.gray));
        iv.setAlpha(active ? 1f : 0.65f);
        tvRef.setTextColor(color(active ? R.color.dark_green : R.color.gray));
    }

    //Parcourt tt les enfants de layoutApplianceIcons, identifie chaque cell et les remet en style inactif
    private void resetAllApplianceViews() {
        for (int i = 0; i < layoutApplianceIcons.getChildCount(); i++) {
            View cell = layoutApplianceIcons.getChildAt(i);
            if (!(cell instanceof LinearLayout)) continue;
            LinearLayout ll = (LinearLayout) cell;
            if (ll.getChildCount() >= 2
                    && ll.getChildAt(0) instanceof ImageView
                    && ll.getChildAt(1) instanceof TextView)
                setApplianceStyle((ImageView) ll.getChildAt(0), (TextView) ll.getChildAt(1), false);
        }
    }

    //Partie reservation:
    //Confirm +post reservation
    @RequiresApi(api = Build.VERSION_CODES.O)
    //branche le listener sur btn confirmer,
    //verify que les 3 elements sont bien selected pour appeler le postReservation
    private void setupConfirmButton() {
        btnConfirm.setOnClickListener(v -> {
            if (selectedDay == null)       { toast(getString(R.string.choose_date));    return; }
            if (selectedTimeslot == null)  { toast(getString(R.string.choose_timeslot));  return; }
            if (selectedAppliance == null) { toast(getString(R.string.choose_appliance)); return; }
            postReservation();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    //appel HTTP POST vers createReservation avc les 4 param necessaires, succes ? ->recup ecocoindelta
    //puis maj du creenUser.solde
    private void postReservation() {
        if (currentUser == null || currentUser.token == null) return;
        Ion.with(getContext()).load("POST", BASE_URL + "createReservation.php")
                .setBodyParameter("token",            currentUser.token)
                .setBodyParameter("reservation_date", selectedDay.getDate())
                .setBodyParameter("timeslot_id",      String.valueOf(selectedTimeslot.getId()))
                .setBodyParameter("appliance_ids",    String.valueOf(selectedAppliance.getId()))
                .asString().setCallback((e, r) -> {
                    if (e != null) { toast(getString(R.string.error_network)); return; }
                    handleJson(r, "eco_coin_delta", delta -> {
                        currentUser.solde += delta;
                        saveUserToSession();
                        hideCreneauSection();
                        loadReservations();
                        loadCalendarStatus();
                        toast(getString(R.string.reservation_confirmed));
                    });
                });
    }

    //Partie mes creneaux:

    @RequiresApi(api = Build.VERSION_CODES.O)
    //Appel HTTP POST vers getReservByUser.parsee a Reservation.getListFromJson() puis passee
    //a renderReservations() pour affichage
    private void loadReservations() {
        if (currentUser == null || currentUser.token == null) return;
        Ion.with(getContext())
                .load(BASE_URL + "getReservationByUser.php?token=" + currentUser.token)
                .asString().setCallback((e, r) -> {
                    if (e != null) { toast(getString(R.string.error_reserv)); return; }
                    renderReservations(Reservation.getListFromJson(r));
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    //construit  la liste mes creneaux de 0 (non pas reutiliser les adapter precedent car on y ajoute les creneaux,
    //et on liste les creneaux by date
    //on filtre les reservations passee
    private void renderReservations(List<Reservation> list) {
        if (layoutReservations == null) return;
        layoutReservations.removeAllViews();
        LocalDate today = LocalDate.now();
        if (list == null || list.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText(R.string.no_upcoming_res);
            tv.setTextColor(color(R.color.gray));
            tv.setTextSize(13f);
            layoutReservations.addView(tv);
            return;
        }
        String lastLabel = null;
        for (Reservation res : list) {
            LocalDate d = LocalDate.parse(res.getReservation_date(), DateTimeFormatter.ISO_LOCAL_DATE);
            //pas de reservation passee
            if (d.isBefore(today)) continue;
            String label = formatDate(res.getReservation_date()) + " :";
            if (!label.equals(lastLabel)) {
                //ne pas afficher deux fois le mm header
                lastLabel = label;
                TextView tvD = new TextView(getContext());
                tvD.setText(label);
                tvD.setTextColor(color(R.color.black));
                tvD.setTextSize(14f);
                tvD.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 14, 0, 6);
                tvD.setLayoutParams(lp);
                layoutReservations.addView(tvD);
            }
            if (res.getAppliances() != null)
                for (Appliance app : res.getAppliances())
                    addReservationCard(res, app, d, today);
            //on cree une card a afficher via addReservationCard()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addReservationCard(Reservation res, Appliance app, LocalDate d, LocalDate today) {
        View card = LayoutInflater.from(getContext())
                .inflate(R.layout.item_reservation, layoutReservations, false);
        ((ImageView) card.findViewById(R.id.iv_res_appliance_icon)).setImageResource(app.getD());
        ((TextView)  card.findViewById(R.id.tv_res_appliance_name)).setText(app.getName());
        ((TextView)  card.findViewById(R.id.tv_res_appliance_ref)).setText(app.getReference());
        ((TextView)  card.findViewById(R.id.tv_res_wattage)).setText(app.getWattage() + " W");
        ((TextView)  card.findViewById(R.id.tv_res_timeslot)).setText(
                res.getTimeslot() != null ? res.getTimeslot().getLabel() : "--");
        ImageView ivDel = card.findViewById(R.id.iv_res_delete);
        boolean canDel = !d.isBefore(today);
        ivDel.setVisibility(canDel ? View.VISIBLE : View.GONE);
        if (canDel) ivDel.setOnClickListener(v -> confirmDelete(res));
        //si clique sur la poubelle -> appelle confirmDelete()
        layoutReservations.addView(card);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    //affiche la boite de dialogue
    private void confirmDelete(Reservation res) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.delete_confirm_booking)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.dialog_delete_title, (d, w) -> deleteReservation(res))
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    //POST vers deleteReservation.php, recup eco coin reverted (delta inverse) maj du solde + refresh
    private void deleteReservation(Reservation res) {
        if (currentUser == null || currentUser.token == null) return;
        Ion.with(getContext()).load("POST", BASE_URL + "deleteReservation.php")
                .setBodyParameter("token",          currentUser.token)
                .setBodyParameter("reservation_id", String.valueOf(res.getId()))
                .asString().setCallback((e, r) -> {
                    if (e != null) { toast(getString(R.string.error_network)); return; }
                    handleJson(r, "eco_coin_reverted", delta -> {
                        currentUser.solde += delta;
                        saveUserToSession();
                        loadReservations();
                        loadCalendarStatus();
                        toast(getString(R.string.delete_reserv));
                    });
                });
    }

    @SuppressLint("SetTextI18n")
    //maj le tvbonusecocoin selon la couleur du creneau selectionne
    private void updateEcoCoinPreview() {
        if (selectedSlotColor == null) {
            tvBonusEcoCoin.setText("--");
            tvBonusEcoCoin.setTextColor(color(R.color.gray));
            tvWarningMessage.setVisibility(View.GONE);
            return;
        }
        int delta = ecoDelta(selectedSlotColor);
        tvWarningMessage.setVisibility("red".equals(selectedSlotColor) ? View.VISIBLE : View.GONE);
        tvBonusEcoCoin.setText(delta > 0 ? "+ " + delta : delta < 0 ? "- " + Math.abs(delta) : "0");
        tvBonusEcoCoin.setTextColor(color(delta > 0 ? R.color.green : delta < 0 ? R.color.red : R.color.gray));
    }

    @SuppressLint("SetTextI18n")
    //maj solde actuel et solde+delta prevu , vert si +, rouge si -, gris si 0
    private void updateSoldeDisplays() {
        if (currentUser == null) return;
        tvSoldeInitial.setText(String.valueOf(currentUser.solde));
        int total = currentUser.solde + (selectedSlotColor != null ? ecoDelta(selectedSlotColor) : 0);
        tvTotalBalance.setText(total > 0 ? "+ " + total : total < 0 ? "- " + Math.abs(total) : "0");
        tvTotalBalance.setTextColor(color(total > 0 ? R.color.green : total < 0 ? R.color.red : R.color.gray));
    }

    //Partie visibility
    private void hideCreneauSection() {
        if (requestCard != null) requestCard.setVisibility(View.GONE);
        selectedDay = null; selectedTimeslot = null; selectedSlotColor = null; selectedAppliance = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showCreneauSection() {
        if (requestCard != null) requestCard.setVisibility(View.VISIBLE);
        setupConfirmButton();
        updateSoldeDisplays();
    }

    //Partie Helpers;
    //couleur -> delta eco coin :  rouge -> -100, jaune -> 0, vert -> +100
    private int ecoDelta(String c) {
        return "red".equals(c) ? -100 : "yellow".equals(c) ? 0 : 100;
    }

    private int colorRes(String c) {
        if ("green".equals(c))  return R.color.green;
        if ("yellow".equals(c)) return R.color.orange;
        if ("red".equals(c))    return R.color.red;
        return R.color.off_white;
    }

    private int color(int res) { return ContextCompat.getColor(requireContext(), res); }

    //Raccourci pour Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show()
    private void toast(String msg) { Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show(); }

    @RequiresApi(api = Build.VERSION_CODES.O)
    //Convertit une date ISO "2026-03-28" en "28/03" pour laffichage dans l'UI
    private String formatDate(String iso) {
        return LocalDate.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE)
                .format(DateTimeFormatter.ofPattern("dd/MM"));
    }

    //serialize currentUser en JSON avec Gson -> SharedPreferences.
    // permet à MonHabitatFragment de lire le solde maj dans son onResume() en gros
    private void saveUserToSession() {
        if (getContext() == null || currentUser == null) return;
        getContext().getSharedPreferences("UserSession", MODE_PRIVATE)
                .edit().putString("user_json", new Gson().toJson(currentUser)).apply();
    }

    //mini interface fonctionnelle qui va prendre un int (Consumer<Integer>) MAIS
    // COMPATIBLE avc les lambdas, handleJson parse la reponse du serv
    //verify que success is true, (evite duplicatastrophe try/catch dans post et deleteReservation
    interface IntCallback { void run(int value); }

    private void handleJson(String r, String key, IntCallback onSuccess) {
        try {
            JSONObject obj = new JSONObject(r);
            if (obj.optBoolean("success")) onSuccess.run(obj.optInt(key, 0));
            else toast(obj.optString("error", getString(R.string.error_generic)));
        } catch (Exception ex) { toast(getString(R.string.error_json)); }
    }
}