package iut.dam.powerhome;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    private final Context context;
    private final List<CalendarDay> days = new ArrayList<>();
    private final OnDayClickListener listener;
    private String selectedDate;

    public CalendarDayAdapter(Context context, OnDayClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setDays(List<CalendarDay> newDays) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        notifyDataSetChanged();
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDay day = days.get(position);

        LocalDate localDate = LocalDate.parse(day.getDate(), DateTimeFormatter.ISO_LOCAL_DATE);
        holder.tvDay.setText(String.valueOf(localDate.getDayOfMonth()));

        int fillColor = getFillColor(day.getColor());
        int textColor = ContextCompat.getColor(context, R.color.white);

        if ("disabled".equals(day.getColor())) {
            textColor = ContextCompat.getColor(context, R.color.gray);
        }

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(fillColor);

        boolean isToday = localDate.equals(LocalDate.now());
        boolean isSelected = day.getDate().equals(selectedDate);

        if (isToday) {
            bg.setStroke(5, ContextCompat.getColor(context, R.color.dark_green));
        } else if (isSelected && !day.isBlocked()) {
            bg.setStroke(4, ContextCompat.getColor(context, R.color.black));
        }

        holder.tvDay.setBackground(bg);
        holder.tvDay.setTextColor(textColor);

        if (day.isBlocked()) {
            holder.itemView.setAlpha(0.75f);
            holder.itemView.setOnClickListener(null);
        } else {
            holder.itemView.setAlpha(1f);
            holder.itemView.setOnClickListener(v -> {
                selectedDate = day.getDate();
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onDayClick(day);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    private int getFillColor(String color) {
        if ("green".equals(color)) {
            return ContextCompat.getColor(context, R.color.green);
        }
        if ("yellow".equals(color)) {
            return ContextCompat.getColor(context, R.color.orange);
        }
        if ("red".equals(color)) {
            return ContextCompat.getColor(context, R.color.red);
        }
        return ContextCompat.getColor(context, R.color.off_white);
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_day_number);
        }
    }
}