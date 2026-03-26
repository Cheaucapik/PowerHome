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

    private LocalDate currentWindowStartDate;
    private CalendarDay selectedDay;
    private CalendarDayAdapter calendarDayAdapter;

    private String selectedSlotColor = "green";
    private final ArrayList<Appliance> selectedAppliances = new ArrayList<>();

    public RequetesFragment() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.requetes_fragment, container, false);

        initViews();
        loadSessionUser();
        initSlots();
        initCalendar();
        renderAppliances(currentUser != null && currentUser.habitat != null ? currentUser.habitat.getAppliances() : null);
        updateEcoCoinPreview();
        updateTotalBalance();

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

        tvSlotMorning = layout.findViewById(R.id.tv_slot_morning);
        tvSlotAfternoon = layout.findViewById(R.id.tv_slot_afternoon);
        tvSlotEvening = layout.findViewById(R.id.tv_slot_evening);

        layoutApplianceIcons = layout.findViewById(R.id.layout_appliance_icons);
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
            selectedDay = day;
            tvSelectedDate.setText(formatDateForUi(day.getDate()));
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

    private void initSlots() {
        tvSlotMorning.setOnClickListener(v -> selectSlot("green"));
        tvSlotAfternoon.setOnClickListener(v -> selectSlot("yellow"));
        tvSlotEvening.setOnClickListener(v -> selectSlot("red"));

        selectSlot("green");
    }

    private void selectSlot(String color) {
        selectedSlotColor = color;

        resetSlotStyle(tvSlotMorning);
        resetSlotStyle(tvSlotAfternoon);
        resetSlotStyle(tvSlotEvening);

        if ("green".equals(color)) {
            applySelectedSlotStyle(tvSlotMorning, R.color.green);
        } else if ("yellow".equals(color)) {
            applySelectedSlotStyle(tvSlotAfternoon, R.color.orange);
        } else {
            applySelectedSlotStyle(tvSlotEvening, R.color.red);
        }

        updateEcoCoinPreview();
        updateTotalBalance();
    }

    private void resetSlotStyle(TextView textView) {
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        textView.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.off_white))
        );
    }

    private void applySelectedSlotStyle(TextView textView, int colorRes) {
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        textView.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), colorRes))
        );
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

                    if (selectedDay == null && !calendarResponse.getDays().isEmpty()) {
                        selectedDay = calendarResponse.getDays().get(0);
                        calendarDayAdapter.setSelectedDate(selectedDay.getDate());
                        tvSelectedDate.setText(formatDateForUi(selectedDay.getDate()));
                    }
                },
                error -> Toast.makeText(getContext(), "Erreur chargement calendrier", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    @SuppressLint("SetTextI18n")
    private void updateEcoCoinPreview() {
        int delta;
        int colorRes;

        switch (selectedSlotColor) {
            case "red":
                delta = -100;
                colorRes = R.color.red;
                break;
            case "yellow":
                delta = 0;
                colorRes = R.color.gray;
                break;
            default:
                delta = 100;
                colorRes = R.color.green;
                break;
        }

        String text = delta > 0 ? "+ " + delta : (delta < 0 ? "- " + Math.abs(delta) : "0");
        tvBonusEcoCoin.setText(text);
        tvBonusEcoCoin.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
    }

    @SuppressLint("SetTextI18n")
    private void updateTotalBalance() {
        if (currentUser == null) return;

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