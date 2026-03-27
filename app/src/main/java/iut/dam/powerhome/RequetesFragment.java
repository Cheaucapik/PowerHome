package iut.dam.powerhome;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
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

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequetesFragment extends Fragment {

    // ── Views ──────────────────────────────────────────────────────────────
    private View layout;
    private TextView tvMonth, btnPrev2Weeks, btnNext2Weeks;
    private RecyclerView rvCalendarDays;

    private LinearLayout requestCard;
    private TextView tvResumeDonnees, tvSelectedDate;
    private TextView tvSlotMorning, tvSlotAfternoon, tvSlotEvening;
    private TextView tvBonusEcoCoin, tvSoldeInitial, tvTotalBalance;
    private TextView tvWarningMessage, btnConfirm;
    private LinearLayout layoutApplianceIcons;

    private LinearLayout layoutReservations;  // "Mes créneaux" container

    // ── State ──────────────────────────────────────────────────────────────
    private User currentUser;
    private LocalDate currentWindowStartDate;
    private CalendarDay selectedDay;
    private CalendarDayAdapter calendarDayAdapter;

    private String selectedSlotColor = null;
    private Timeslot selectedTimeslot = null;
    private Appliance selectedAppliance = null;   // only ONE at a time

    // ── URLs ───────────────────────────────────────────────────────────────
    private static final String BASE_URL = "http://10.0.2.2/powerhome_server/";

    public RequetesFragment() {}

    // ══════════════════════════════════════════════════════════════════════
    //  Lifecycle
    // ══════════════════════════════════════════════════════════════════════
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.requetes_fragment, container, false);

        initViews();
        loadSessionUser();
        initCalendar();
        renderAppliances(currentUser != null && currentUser.habitat != null
                ? currentUser.habitat.getAppliances() : null);

        hideCreneauSection();
        loadReservations();

        return layout;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Init
    // ══════════════════════════════════════════════════════════════════════
    private void initViews() {
        tvMonth          = layout.findViewById(R.id.tv_month);
        btnPrev2Weeks    = layout.findViewById(R.id.btn_prev_2weeks);
        btnNext2Weeks    = layout.findViewById(R.id.btn_next_2weeks);
        rvCalendarDays   = layout.findViewById(R.id.rv_calendar_days);

        requestCard      = layout.findViewById(R.id.request_card);
        tvResumeDonnees  = layout.findViewById(R.id.tv_resume_donnees);
        tvSelectedDate   = layout.findViewById(R.id.tv_selected_date);
        tvSlotMorning    = layout.findViewById(R.id.tv_slot_morning);
        tvSlotAfternoon  = layout.findViewById(R.id.tv_slot_afternoon);
        tvSlotEvening    = layout.findViewById(R.id.tv_slot_evening);
        tvBonusEcoCoin   = layout.findViewById(R.id.tv_bonus_ecocoin);
        tvSoldeInitial   = layout.findViewById(R.id.tv_solde_initial);
        tvTotalBalance   = layout.findViewById(R.id.tv_total_balance);
        tvWarningMessage = layout.findViewById(R.id.tv_warning_message);
        btnConfirm       = layout.findViewById(R.id.btn_confirm);
        layoutApplianceIcons = layout.findViewById(R.id.layout_appliance_icons);
        layoutReservations   = layout.findViewById(R.id.layout_reservations);
    }

    private void loadSessionUser() {
        if (getContext() == null) return;
        SharedPreferences sp = getContext().getSharedPreferences("UserSession", MODE_PRIVATE);
        String json = sp.getString("user_json", null);
        if (json == null) {
            Toast.makeText(getContext(), "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUser = User.getFromJson(json);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Calendar
    // ══════════════════════════════════════════════════════════════════════
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initCalendar() {
        currentWindowStartDate = LocalDate.now();

        calendarDayAdapter = new CalendarDayAdapter(requireContext(), day -> {
            selectedDay = day;
            selectedTimeslot = null;
            selectedSlotColor = null;

            showCreneauSection();
            tvSelectedDate.setText(formatDateForUi(day.getDate()));
            updateSlotsFromDay(day);
            updateEcoCoinPreview();
            updateSoldeDisplays();
        });

        rvCalendarDays.setLayoutManager(new GridLayoutManager(getContext(), 7));
        rvCalendarDays.setAdapter(calendarDayAdapter);

        btnPrev2Weeks.setOnClickListener(v -> {
            currentWindowStartDate = currentWindowStartDate.minusDays(14);
            loadCalendarStatus();
        });
        btnNext2Weeks.setOnClickListener(v -> {
            currentWindowStartDate = currentWindowStartDate.plusDays(14);
            loadCalendarStatus();
        });

        loadCalendarStatus();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadCalendarStatus() {
        if (currentUser == null || currentUser.token == null) return;

        String startDate = currentWindowStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String url = BASE_URL + "getCalendarStatus.php?token=" + currentUser.token
                + "&start_date=" + startDate;

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    CalendarStatusResponse cr = CalendarStatusResponse.getFromJson(response);
                    if (cr == null || cr.getDays() == null) return;
                    tvMonth.setText(cr.getMonth_label());
                    calendarDayAdapter.setDays(cr.getDays());
                },
                error -> Toast.makeText(getContext(), "Erreur chargement calendrier", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(requireContext()).add(req);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Slot handling
    // ══════════════════════════════════════════════════════════════════════
    private void updateSlotsFromDay(CalendarDay day) {
        List<Timeslot> slots = day != null ? day.getSlots() : null;
        Timeslot morning = null, afternoon = null, evening = null;
        if (slots != null) {
            for (Timeslot t : slots) {
                if (t.getSlot_order() == 1) morning = t;
                else if (t.getSlot_order() == 2) afternoon = t;
                else if (t.getSlot_order() == 3) evening = t;
            }
        }
        applySlotAppearance(tvSlotMorning, morning, false);
        applySlotAppearance(tvSlotAfternoon, afternoon, false);
        applySlotAppearance(tvSlotEvening, evening, false);

        final Timeslot fm = morning, fa = afternoon, fe = evening;
        tvSlotMorning.setOnClickListener(v -> selectSlot(fm, tvSlotMorning));
        tvSlotAfternoon.setOnClickListener(v -> selectSlot(fa, tvSlotAfternoon));
        tvSlotEvening.setOnClickListener(v -> selectSlot(fe, tvSlotEvening));
    }

    private void applySlotAppearance(TextView tv, Timeslot t, boolean isSelected) {
        String color = (t != null) ? t.getColor() : null;
        if (color == null || color.isEmpty()) {
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
            tv.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.off_white)));
            tv.setAlpha(1f);
            return;
        }
        int bgRes;
        switch (color) {
            case "green":  bgRes = R.color.green;  break;
            case "yellow": bgRes = R.color.orange; break;
            case "red":    bgRes = R.color.red;    break;
            default:       bgRes = R.color.off_white; break;
        }
        if (isSelected) {
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            tv.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), bgRes)));
            tv.setAlpha(1f);
        } else {
            // Ghost: colored text, neutral bg
            tv.setTextColor(ContextCompat.getColor(requireContext(), bgRes));
            tv.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.off_white)));
            tv.setAlpha(0.85f);
        }
    }

    private void selectSlot(Timeslot t, TextView tappedView) {
        selectedTimeslot = t;
        selectedSlotColor = (t != null && t.getColor() != null) ? t.getColor() : "green";

        // Re-render all 3 slots
        List<Timeslot> slots = selectedDay != null ? selectedDay.getSlots() : null;
        Timeslot morning = null, afternoon = null, evening = null;
        if (slots != null) {
            for (Timeslot ts : slots) {
                if (ts.getSlot_order() == 1) morning = ts;
                else if (ts.getSlot_order() == 2) afternoon = ts;
                else if (ts.getSlot_order() == 3) evening = ts;
            }
        }
        applySlotAppearance(tvSlotMorning,   morning,   tappedView == tvSlotMorning);
        applySlotAppearance(tvSlotAfternoon, afternoon, tappedView == tvSlotAfternoon);
        applySlotAppearance(tvSlotEvening,   evening,   tappedView == tvSlotEvening);

        updateEcoCoinPreview();
        updateSoldeDisplays();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Appliances (single selection)
    // ══════════════════════════════════════════════════════════════════════
    private void renderAppliances(List<Appliance> appliances) {
        if (layoutApplianceIcons == null) return;
        layoutApplianceIcons.removeAllViews();
        selectedAppliance = null;
        if (appliances == null) return;

        for (Appliance appliance : appliances) {
            // Vertical container: icon + reference label
            LinearLayout cell = new LinearLayout(getContext());
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cellParams.setMargins(0, 0, 28, 0);
            cell.setLayoutParams(cellParams);

            // Icon — convert 64dp → px for consistent sizing across densities
            int iconSizePx = (int) (64 * getResources().getDisplayMetrics().density);
            ImageView imageView = new ImageView(getContext());
            imageView.setImageResource(appliance.getD());
            imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray));
            imageView.setAlpha(0.65f);
            imageView.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSizePx, iconSizePx);
            imageView.setLayoutParams(iconParams);
            imageView.setPadding(8, 8, 8, 8);

            // Reference label below icon
            TextView tvRef = new TextView(getContext());
            tvRef.setText(appliance.getReference());
            tvRef.setTextSize(12f);
            tvRef.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
            tvRef.setGravity(android.view.Gravity.CENTER);

            cell.addView(imageView);
            cell.addView(tvRef);

            cell.setOnClickListener(v -> toggleSingleAppliance(appliance, imageView, tvRef, appliances));
            layoutApplianceIcons.addView(cell);
        }
    }

    private void toggleSingleAppliance(Appliance appliance, ImageView icon, TextView tvRef,
                                       List<Appliance> allAppliances) {
        if (selectedAppliance != null && selectedAppliance.getId() == appliance.getId()) {
            // Deselect
            selectedAppliance = null;
            icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray));
            icon.setAlpha(0.65f);
            tvRef.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        } else {
            // Deselect previous (reset all)
            resetAllApplianceViews();
            selectedAppliance = appliance;
            icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_green));
            icon.setAlpha(1f);
            tvRef.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_green));
        }
    }

    private void resetAllApplianceViews() {
        for (int i = 0; i < layoutApplianceIcons.getChildCount(); i++) {
            View cell = layoutApplianceIcons.getChildAt(i);
            if (!(cell instanceof LinearLayout)) continue;
            LinearLayout ll = (LinearLayout) cell;
            if (ll.getChildCount() < 2) continue;
            View v0 = ll.getChildAt(0);
            View v1 = ll.getChildAt(1);
            if (v0 instanceof ImageView) {
                ((ImageView) v0).setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray));
                v0.setAlpha(0.65f);
            }
            if (v1 instanceof TextView) {
                ((TextView) v1).setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Confirm reservation
    // ══════════════════════════════════════════════════════════════════════
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupConfirmButton() {
        btnConfirm.setOnClickListener(v -> {
            if (selectedDay == null) {
                Toast.makeText(getContext(), "Choisissez une date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedTimeslot == null) {
                Toast.makeText(getContext(), "Choisissez un créneau", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedAppliance == null) {
                Toast.makeText(getContext(), "Choisissez un appareil", Toast.LENGTH_SHORT).show();
                return;
            }
            postReservation();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void postReservation() {
        if (currentUser == null || currentUser.token == null) return;

        String url = BASE_URL + "createReservation.php";
        final String date      = selectedDay.getDate();
        final int    timeslotId = selectedTimeslot.getId();
        final int    applianceId = selectedAppliance.getId();

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Success → close créneau section, refresh reservations, update solde in memory
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response);
                        if (obj.optBoolean("success")) {
                            int delta = obj.optInt("eco_coin_delta", 0);
                            if (currentUser != null) {
                                currentUser.solde += delta;
                                saveUserToSession();
                            }
                            hideCreneauSection();
                            loadReservations();
                            loadCalendarStatus(); // refresh colors
                            Toast.makeText(getContext(), "Réservation confirmée !", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(),
                                    obj.optString("error", "Erreur"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Erreur de réponse", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Erreur réseau", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("token", currentUser.token);
                p.put("reservation_date", date);
                p.put("timeslot_id", String.valueOf(timeslotId));
                p.put("appliance_ids", String.valueOf(applianceId));
                return p;
            }
        };
        Volley.newRequestQueue(requireContext()).add(req);
    }

    private void saveUserToSession() {
        if (getContext() == null || currentUser == null) return;
        SharedPreferences sp = getContext().getSharedPreferences("UserSession", MODE_PRIVATE);
        sp.edit().putString("user_json", new com.google.gson.Gson().toJson(currentUser)).apply();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Mes créneaux – load & render
    // ══════════════════════════════════════════════════════════════════════
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadReservations() {
        if (currentUser == null || currentUser.token == null) return;

        String url = BASE_URL + "getReservationByUser.php?token=" + currentUser.token;

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    List<Reservation> reservations = Reservation.getListFromJson(response);
                    renderReservations(reservations);
                },
                error -> Toast.makeText(getContext(), "Erreur chargement réservations", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(requireContext()).add(req);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void renderReservations(List<Reservation> reservations) {
        if (layoutReservations == null) return;
        layoutReservations.removeAllViews();

        if (reservations == null || reservations.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("Aucune réservation à venir.");
            empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
            empty.setTextSize(13f);
            layoutReservations.addView(empty);
            return;
        }

        LocalDate today = LocalDate.now();
        String currentDateLabel = null;

        for (Reservation res : reservations) {
            // Skip past reservations
            LocalDate resDate = LocalDate.parse(res.getReservation_date(), DateTimeFormatter.ISO_LOCAL_DATE);
            if (resDate.isBefore(today)) continue;

            // Date header (group by date)
            String dateLabel = "Le " + formatDateForUi(res.getReservation_date()) + " :";
            if (!dateLabel.equals(currentDateLabel)) {
                currentDateLabel = dateLabel;
                TextView tvDate = new TextView(getContext());
                tvDate.setText(dateLabel);
                tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                tvDate.setTextSize(14f);
                tvDate.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 14, 0, 6);
                tvDate.setLayoutParams(lp);
                layoutReservations.addView(tvDate);
            }

            // One card per appliance in the reservation
            if (res.getAppliances() != null) {
                for (iut.dam.powerhome.Appliance app : res.getAppliances()) {
                    addReservationCard(res, app, resDate, today);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addReservationCard(Reservation res, iut.dam.powerhome.Appliance app,
                                    LocalDate resDate, LocalDate today) {
        View cardView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_reservation, layoutReservations, false);

        ImageView ivIcon   = cardView.findViewById(R.id.iv_res_appliance_icon);
        TextView  tvName   = cardView.findViewById(R.id.tv_res_appliance_name);
        TextView  tvRef    = cardView.findViewById(R.id.tv_res_appliance_ref);
        TextView  tvWatt   = cardView.findViewById(R.id.tv_res_wattage);
        TextView  tvSlot   = cardView.findViewById(R.id.tv_res_timeslot);
        ImageView ivDelete = cardView.findViewById(R.id.iv_res_delete);

        ivIcon.setImageResource(app.getD());
        tvName.setText(app.getName());
        tvRef.setText(app.getReference());
        tvWatt.setText(app.getWattage() + " W");
        tvSlot.setText(res.getTimeslot() != null ? res.getTimeslot().getLabel() : "--");

        // Delete only if reservation is not yet passed
        boolean canDelete = !resDate.isBefore(today);
        ivDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);

        if (canDelete) {
            ivDelete.setOnClickListener(v -> showDeleteDialog(res));
        }

        layoutReservations.addView(cardView);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showDeleteDialog(Reservation res) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Supprimer")
                .setMessage("Voulez-vous vraiment supprimer cette réservation ?")
                .setNegativeButton("ANNULER", null)
                .setPositiveButton("SUPPRIMER", (dialog, which) -> deleteReservation(res))
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void deleteReservation(Reservation res) {
        if (currentUser == null || currentUser.token == null) return;

        String url = BASE_URL + "deleteReservation.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response);
                        if (obj.optBoolean("success")) {
                            int reverted = obj.optInt("eco_coin_reverted", 0);
                            if (currentUser != null) {
                                currentUser.solde += reverted;
                                saveUserToSession();
                            }
                            loadReservations();
                            loadCalendarStatus();
                            Toast.makeText(getContext(), "Réservation supprimée.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(),
                                    obj.optString("error", "Erreur suppression"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Erreur de réponse", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Erreur réseau", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("token", currentUser.token);
                p.put("reservation_id", String.valueOf(res.getId()));
                return p;
            }
        };
        Volley.newRequestQueue(requireContext()).add(req);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Eco-coin & solde displays
    // ══════════════════════════════════════════════════════════════════════
    @SuppressLint("SetTextI18n")
    private void updateEcoCoinPreview() {
        if (selectedSlotColor == null) {
            tvBonusEcoCoin.setText("--");
            tvBonusEcoCoin.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
            tvWarningMessage.setVisibility(View.GONE);
            return;
        }
        int delta;
        int colorRes;
        switch (selectedSlotColor) {
            case "red":
                delta = -100; colorRes = R.color.red;
                tvWarningMessage.setVisibility(View.VISIBLE);
                break;
            case "yellow":
                delta = 0; colorRes = R.color.gray;
                tvWarningMessage.setVisibility(View.GONE);
                break;
            default:
                delta = 100; colorRes = R.color.green;
                tvWarningMessage.setVisibility(View.GONE);
                break;
        }
        String text = delta > 0 ? "+ " + delta : (delta < 0 ? "- " + Math.abs(delta) : "0");
        tvBonusEcoCoin.setText(text);
        tvBonusEcoCoin.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
    }

    @SuppressLint("SetTextI18n")
    private void updateSoldeDisplays() {
        if (currentUser == null) return;

        // Solde initial (current value in DB, before this new reservation)
        tvSoldeInitial.setText(String.valueOf(currentUser.solde));

        if (selectedSlotColor == null) {
            tvTotalBalance.setText(String.valueOf(currentUser.solde));
            tvTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
            return;
        }
        int delta;
        switch (selectedSlotColor) {
            case "red":    delta = -100; break;
            case "yellow": delta = 0;   break;
            default:       delta = 100; break;
        }
        int total = currentUser.solde + delta;
        if (total > 0) {
            tvTotalBalance.setText("+ " + total);
            tvTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
        } else if (total < 0) {
            tvTotalBalance.setText("- " + Math.abs(total));
            tvTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
        } else {
            tvTotalBalance.setText("0");
            tvTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Visibility helpers
    // ══════════════════════════════════════════════════════════════════════
    private void hideCreneauSection() {
        if (requestCard != null) requestCard.setVisibility(View.GONE);
        selectedDay = null;
        selectedTimeslot = null;
        selectedSlotColor = null;
        selectedAppliance = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showCreneauSection() {
        if (requestCard != null) requestCard.setVisibility(View.VISIBLE);
        setupConfirmButton();
        updateSoldeDisplays();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════════════════════════════════
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String formatDateForUi(String isoDate) {
        LocalDate date = LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE);
        return date.format(DateTimeFormatter.ofPattern("dd/MM"));
    }
}