package iut.dam.powerhome;

import android.content.Context;
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
        if (newDays != null) days.addAll(newDays);
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
        LocalDate today = LocalDate.now();

        boolean isPast    = localDate.isBefore(today);
        boolean isToday   = localDate.equals(today);
        boolean isSelected = day.getDate().equals(selectedDate);

        holder.tvDay.setText(String.valueOf(localDate.getDayOfMonth()));

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);

        if (isPast) {
            // Past dates: grey fill, no interaction
            bg.setColor(ContextCompat.getColor(context, R.color.off_white));
            holder.tvDay.setTextColor(ContextCompat.getColor(context, R.color.gray));
            holder.tvDay.setAlpha(0.5f);
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);

        } else if (isToday && isSelected) {
            // Today AND selected: green fill + black outer ring
            bg.setColor(ContextCompat.getColor(context, R.color.dark_green));
            bg.setStroke(4, ContextCompat.getColor(context, R.color.black));
            holder.tvDay.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.tvDay.setAlpha(1f);
            setClickListener(holder, day);

        } else if (isToday) {
            // Today only: dark_green stroke, no fill (or light tint)
            bg.setColor(ContextCompat.getColor(context, R.color.off_white));
            bg.setStroke(4, ContextCompat.getColor(context, R.color.dark_green));
            holder.tvDay.setTextColor(ContextCompat.getColor(context, R.color.dark_green));
            holder.tvDay.setAlpha(1f);
            setClickListener(holder, day);

        } else if (isSelected) {
            // Selected (not today): colored fill from affluence + highlighted surbrillance ring
            int fillColor = getFillColor(day.getColor());
            bg.setColor(fillColor);
            // White inner ring to create a "surbrillance" effect
            bg.setStroke(3, ContextCompat.getColor(context, R.color.white));
            holder.tvDay.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.tvDay.setAlpha(1f);
            setClickListener(holder, day);

        } else {
            // Normal future date: colored fill based on affluence
            int fillColor = getFillColor(day.getColor());
            bg.setColor(fillColor);
            int textColor = "disabled".equals(day.getColor())
                    ? ContextCompat.getColor(context, R.color.gray)
                    : ContextCompat.getColor(context, R.color.white);
            holder.tvDay.setTextColor(textColor);
            holder.tvDay.setAlpha(day.isBlocked() ? 0.5f : 1f);
            setClickListener(holder, day);
        }

        holder.tvDay.setBackground(bg);
    }

    private void setClickListener(DayViewHolder holder, CalendarDay day) {
        holder.itemView.setClickable(true);
        holder.itemView.setOnClickListener(v -> {
            selectedDate = day.getDate();
            notifyDataSetChanged();
            if (listener != null) listener.onDayClick(day);
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    private int getFillColor(String color) {
        if ("green".equals(color))  return ContextCompat.getColor(context, R.color.green);
        if ("yellow".equals(color)) return ContextCompat.getColor(context, R.color.orange);
        if ("red".equals(color))    return ContextCompat.getColor(context, R.color.red);
        // null / unknown → neutral off_white circle
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