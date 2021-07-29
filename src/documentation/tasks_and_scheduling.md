When task scheduling occurs:

- on application launch:
    - when loading in events from JSON file, each event's `nextSignupExecuted` flag gets checked; 
        - if `false`, then a task for the event gets scheduled;
        - else the event gets put into the `processedEvents` list:
            - if the event is marked as recurring, then put in a renewal task
    
- on event change:
    - when an event's date/time/recurring gets modified by the user, a task containing the event gets scheduled 
        - and the previous task for the event, if applicable, gets removed
    
- on event execution:
    - if the event is marked as recurring, then on task execution for the event:
        - a renewal task for the event will be scheduled for right after the event executes
        - set `nextSignupExecuted` flag to true
        
At any time, each event should only have at most 1 task scheduled, be it a signup task or a renewal task.
    
Task types:
    - signup task
        - sign up lol
    - renewal task
        - increment event date by a week, set `nextSignupExecuted` flag to `false`, and schedule the signup task (happens 2 days minus 1 min before event starts)
        - renewal task essentially is a pre-signup task, as we don't want to sign up for the next event before the upcoming one is over