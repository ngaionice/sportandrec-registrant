package scheduler;

import javafx.beans.property.StringProperty;
import model.DataModel;
import model.Event;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    ScheduledThreadPoolExecutor executor;
    DataModel model;

    public Scheduler(DataModel model) {
        this.executor = new ScheduledThreadPoolExecutor(2);
        executor.setRemoveOnCancelPolicy(true);
        this.model = model;
    }

    /**
     * Returns the number of minutes between the current time and the signup time of the event.
     */
    public static long getMinutesFromSignupTime(LocalDateTime happeningTime) {
        LocalDateTime signUpTime = getSignupTime(happeningTime);
        return Duration.between(LocalDateTime.now(), signUpTime).toMinutes();
    }

    /**
     * Returns the number of minutes between the current time and the time at which the event happens.
     */
    public static long getMinutesFromHappeningTime(LocalDateTime happeningTime) {
        return Duration.between(LocalDateTime.now(), happeningTime).toMinutes();
    }

    public static LocalDateTime getSignupTime(LocalDateTime happeningTime) {
        return happeningTime.minus(2, ChronoUnit.DAYS).minus(1, ChronoUnit.MINUTES);
    }

    /**
     * Schedules a signup task that executes immediately or 2 days - 1 minute before the event time, whichever has a smaller time difference between event time and signup time.
     * If the event time has passed/is right now, does nothing.
     */
    public void scheduleSignupTask(Event e) {
        LocalDateTime happeningTime = LocalDateTime.of(e.getNextDate(), e.getTime());
        if (happeningTime.isBefore(LocalDateTime.now())) return;
        // if signup has already opened, sign up now, else sign up 1 min after registration opens
        SignupTask task = new SignupTask(executor, e, model.usernameProperty(), model.passwordProperty());
        if (getSignupTime(happeningTime).isBefore(LocalDateTime.now())) executor.execute(task);
        else {
            executor.schedule(task, getMinutesFromSignupTime(happeningTime), TimeUnit.MINUTES);
            System.out.println("scheduled task: " + e.getId());
        }
    }

    /**
     * Schedules a renewal task that executes right after the event occurs.
     */
    public void scheduleRenewalTask(Event e) {
        LocalDateTime happeningTime = LocalDateTime.of(e.getNextDate(), e.getTime());
        long waitingTime = getMinutesFromHappeningTime(happeningTime); // how long until current event occurs
        RenewalTask task = new RenewalTask(executor, e, model.usernameProperty(), model.passwordProperty());
        if (waitingTime <= 0) executor.execute(task);
        else executor.schedule(task, waitingTime, TimeUnit.MINUTES);
    }

    private static class SignupTask implements Runnable {

        private final Registrant r;
        private final Event e;
        private final StringProperty userId;
        private final StringProperty password;
        private final ScheduledThreadPoolExecutor executor;

        SignupTask(ScheduledThreadPoolExecutor executor, Event e, StringProperty userId, StringProperty password) {
            r = new Registrant(userId, password, e.getUrl(), e.getNextDate(), e.getTime(), false);
            this.e = e;
            this.userId = userId;
            this.password = password;
            this.executor = executor;
        }

        @Override
        public void run() {
            r.initialize();
            r.login();
            r.registerEvent();
            r.end();
            e.setNextSignupExecuted(true);
            if (e.isRecurring()) {
                LocalDateTime happeningTime = LocalDateTime.of(e.getNextDate(), e.getTime());
                executor.schedule(new RenewalTask(executor, e, userId, password), Scheduler.getMinutesFromHappeningTime(happeningTime), TimeUnit.MINUTES);
            }
        }
    }

    private static class RenewalTask implements Runnable {

        private final Event e;
        private final StringProperty userId;
        private final StringProperty password;
        private final ScheduledThreadPoolExecutor executor;

        RenewalTask(ScheduledThreadPoolExecutor executor, Event e, StringProperty userId, StringProperty password) {
            this.e = e;
            this.userId = userId;
            this.password = password;
            this.executor = executor;
        }

        @Override
        public void run() {
            e.setNextDate(e.getNextDate().plusDays(7));
            e.setNextSignupExecuted(false);
            executor.schedule(new SignupTask(executor, e, userId, password), Scheduler.getMinutesFromSignupTime(LocalDateTime.of(e.getNextDate(), e.getTime())), TimeUnit.MINUTES);
        }
    }
}
