package iut.dam.powerhome;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
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
import java.util.List;

public class RequetesFragment extends Fragment {

    private LinearLayout requestCard;
    private TextView tvWarningMessage;

    private View layout;
    private User currentUser;

    private TextView tvMonth;
    private TextView btnPrev2Weeks;
    private TextView btnNext2Weeks;
    private RecyclerView rvCalendarDays;

    private TextView tvSelectedDate;
    private TextView tvBonusEcoCoin;
    private TextView tvTotalBalance;
    private TextView tvSlotMorning;
    private TextView tvSlotAfternoon;
    private TextView tvSlotEvening;
    private LinearLayout layoutApplianceIcons;

    // "Résumé des données" subtitle
    private TextView tvResumeDonnees;

    private LocalDate currentWindowStartDate;
    private CalendarDay selectedDay;
    private CalendarDayAdapter calendarDayAdapter;

    // selectedSlotColor reflects the actual color of the chosen timeslot
    private String selectedSlotColor = null; // null = no slot selected yet
    private Timeslot selectedTimeslot = null;
    private final ArrayList<Appliance> selectedAppliances = new ArrayList<>();

    public RequetesFragment() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.requetes_fragment, container, false);

        initViews();
        loadSessionUser();
        initCalendar();
        renderAppliances(currentUser != null && currentUser.habitat != null ? currentUser.habitat.getAppliances() : null);

        // Initially hide the créneau section — only shown after a date is clicked
        hideCreneauSection();

        return layout;
    }

    private void initViews() {
        tvMonth = layout.findViewById(R.id.tv_month);
        btnPrev2Weeks = layout.findViewById(R.id.btn_prev_2weeks);
        btnNext2Weeks = layout.findViewById(R.id.btn_next_2weeks);
        rvCalendarDays = layout.findViewById(R.id.rv_calendar_days);

        tvSelectedDate = layout.findViewById(R.id.tv_selected_date);
        tvBonusEcoCoin = layout.findViewById(R.id.tv_bonus_ecocoin);
        tvTotalBalance = layout.findViewById(R.id.tv_total_balance);
        tvWarningMessage = layout.findViewById(R.id.tv_warning_message);
        tvResumeDonnees = layout.findViewById(R.id.tv_resume_donnees);

        tvSlotMorning = layout.findViewById(R.id.tv_slot_morning);
        tvSlotAfternoon = layout.findViewById(R.id.tv_slot_afternoon);
        tvSlotEvening = layout.findViewById(R.id.tv_slot_evening);

        layoutApplianceIcons = layout.findViewById(R.id.layout_appliance_icons);

        requestCard = layout.findViewById(R.id.request_card);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initCalendar() {
        currentWindowStartDate = LocalDate.now();

        calendarDayAdapter = new CalendarDayAdapter(requireContext(), day -> {
            // A day was clicked
            selectedDay = day;
            selectedTimeslot = null;
            selectedSlotColor = null;

            showCreneauSection();
            tvSelectedDate.setText(formatDateForUi(day.getDate()));
            updateSlotsFromDay(day);
            updateEcoCoinPreview();
            updateTotalBalance();
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

    /**
     * Update the three slot buttons with colors reflecting the actual affluence
     * for the selected day. If a day has no slot data, all slots appear neutral.
     */
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

        // Store references for click handlers
        final Timeslot finalMorning = morning;
        final Timeslot finalAfternoon = afternoon;
        final Timeslot finalEvening = evening;

        tvSlotMorning.setOnClickListener(v -> selectSlot(finalMorning, tvSlotMorning));
        tvSlotAfternoon.setOnClickListener(v -> selectSlot(finalAfternoon, tvSlotAfternoon));
        tvSlotEvening.setOnClickListener(v -> selectSlot(finalEvening, tvSlotEvening));
    }

    /**
     * Apply the visual appearance of a slot button based on its color/affluence.
     * isSelected: if true, adds a selected ring/highlight.
     */
    private void applySlotAppearance(TextView tv, Timeslot timeslot, boolean isSelected) {
        String color = timeslot != null ? timeslot.getColor() : null;

        int bgColorRes;
        int textColorRes;

        if (color == null || color.isEmpty()) {
            // No data → neutral grey appearance
            bgColorRes = R.color.off_white;
            textColorRes = R.color.gray;
        } else {
            switch (color) {
                case "green":
                    bgColorRes = isSelected ? R.color.green : R.color.green;
                    textColorRes = R.color.white;
                    break;
                case "yellow":
                    bgColorRes = isSelected ? R.color.orange : R.color.orange;
                    textColorRes = R.color.white;
                    break;
                case "red":
                    bgColorRes = isSelected ? R.color.red : R.color.red;
                    textColorRes = R.color.white;
                    break;
                default:
                    bgColorRes = R.color.off_white;
                    textColorRes = R.color.gray;
            }
        }

        if (isSelected) {
            // Selected: full solid color, white text
            tv.setTextColor(ContextCompat.getColor(requireContext(), textColorRes));
            tv.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), bgColorRes)));
            tv.setAlpha(1f);
        } else if (color == null || color.isEmpty()) {
            // Neutral unselected
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
            tv.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.off_white)));
            tv.setAlpha(1f);
        } else {
            // Colored but not selected → show color with reduced opacity (ghost style)
            tv.setTextColor(ContextCompat.getColor(requireContext(), bgColorRes));
            tv.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.off_white)));
            tv.setAlpha(0.85f);
        }
    }

    /**
     * Called when user taps a slot button.
     */
    private void selectSlot(Timeslot timeslot, TextView tappedView) {
        selectedTimeslot = timeslot;
        selectedSlotColor = (timeslot != null && timeslot.getColor() != null)
                ? timeslot.getColor() : "green";

        // Re-render all three slots, highlight only the tapped one
        List<Timeslot> slots = selectedDay != null ? selectedDay.getSlots() : null;
        Timeslot morning = null, afternoon = null, evening = null;
        if (slots != null) {
            for (Timeslot t : slots) {
                if (t.getSlot_order() == 1) morning = t;
                else if (t.getSlot_order() == 2) afternoon = t;
                else if (t.getSlot_order() == 3) evening = t;
            }
        }

        applySlotAppearance(tvSlotMorning, morning, tappedView == tvSlotMorning);
        applySlotAppearance(tvSlotAfternoon, afternoon, tappedView == tvSlotAfternoon);
        applySlotAppearance(tvSlotEvening, evening, tappedView == tvSlotEvening);

        updateEcoCoinPreview();
        updateTotalBalance();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadCalendarStatus() {
        if (currentUser == null || currentUser.token == null) return;

        String startDate = currentWindowStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String url = "http://10.0.2.2/powerhome_server/getCalendarStatus.php?token="
                + currentUser.token + "&start_date=" + startDate;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    CalendarStatusResponse calendarResponse = CalendarStatusResponse.getFromJson(response);
                    if (calendarResponse == null || calendarResponse.getDays() == null) return;

                    tvMonth.setText(calendarResponse.getMonth_label());
                    calendarDayAdapter.setDays(calendarResponse.getDays());
                },
                error -> Toast.makeText(getContext(), "Erreur chargement calendrier", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

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
                delta = -100;
                colorRes = R.color.red;
                tvWarningMessage.setVisibility(View.VISIBLE);
                break;
            case "yellow":
                delta = 0;
                colorRes = R.color.gray;
                tvWarningMessage.setVisibility(View.GONE);
                break;
            default: // green
                delta = 100;
                colorRes = R.color.green;
                tvWarningMessage.setVisibility(View.GONE);
                break;
        }

        String text = delta > 0 ? "+ " + delta : (delta < 0 ? "- " + Math.abs(delta) : "0");
        tvBonusEcoCoin.setText(text);
        tvBonusEcoCoin.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
    }

    @SuppressLint("SetTextI18n")
    private void updateTotalBalance() {
        if (currentUser == null || selectedSlotColor == null) {
            tvTotalBalance.setText("--");
            tvTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
            return;
        }

        int delta;
        switch (selectedSlotColor) {
            case "red":
                delta = -100;
                break;
            case "yellow":
                delta = 0;
                break;
            default:
                delta = 100;
                break;
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

    private void hideCreneauSection() {
        if (requestCard != null) requestCard.setVisibility(View.GONE);
    }

    private void showCreneauSection() {
        if (requestCard != null) requestCard.setVisibility(View.VISIBLE);
        if (tvResumeDonnees != null) tvResumeDonnees.setVisibility(View.VISIBLE);
    }

    private void renderAppliances(List<Appliance> appliances) {
        if (layoutApplianceIcons == null) return;

        layoutApplianceIcons.removeAllViews();
        selectedAppliances.clear();

        if (appliances == null) return;

        for (Appliance appliance : appliances) {
            ImageView imageView = new ImageView(getContext());
            imageView.setImageResource(appliance.getD());
            imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray));
            imageView.setAlpha(0.65f);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(95, 95);
            params.setMargins(0, 0, 26, 0);
            imageView.setLayoutParams(params);
            imageView.setPadding(10, 10, 10, 10);

            imageView.setOnClickListener(v -> toggleApplianceSelection(appliance, imageView));
            layoutApplianceIcons.addView(imageView);
        }
    }

    private void toggleApplianceSelection(Appliance appliance, ImageView imageView) {
        if (selectedAppliances.contains(appliance)) {
            selectedAppliances.remove(appliance);
            imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray));
            imageView.setAlpha(0.65f);
        } else {
            selectedAppliances.add(appliance);
            imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_green));
            imageView.setAlpha(1f);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String formatDateForUi(String isoDate) {
        LocalDate date = LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE);
        return date.format(DateTimeFormatter.ofPattern("dd/MM"));
    }
}