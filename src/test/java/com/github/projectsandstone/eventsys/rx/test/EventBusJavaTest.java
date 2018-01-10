package com.github.projectsandstone.eventsys.rx.test;

import com.github.projectsandstone.eventsys.event.Event;
import com.github.projectsandstone.eventsys.event.EventManager;
import com.github.projectsandstone.eventsys.event.annotation.Name;
import com.github.projectsandstone.eventsys.impl.DefaultEventManager;
import com.github.projectsandstone.eventsys.rx.Events;
import com.github.projectsandstone.eventsys.rx.impl.EventsImpl;

import org.junit.Assert;
import org.junit.Test;

public class EventBusJavaTest {
    @Test
    public void eventBusJavaTest() {
        EventManager manager = new DefaultEventManager();
        Events events = new EventsImpl(manager);
        MyEventFactory myEventFactory = manager.getEventGenerator().createFactory(MyEventFactory.class);

        events.observable(ConnectEvent.class)
                .map(ConnectEvent::getUser)
                .filter(user -> user.getAge() >= 18)
                .subscribe(user -> Assert.assertEquals("UserB", user.getName()));

        manager.dispatch(myEventFactory.createConnectEvent(new User("UserA", 10)), this);
        manager.dispatch(myEventFactory.createConnectEvent(new User("UserB", 18)), this);
    }

    public interface MyEventFactory {
        ConnectEvent createConnectEvent(@Name("user") User user);
    }

    public interface ConnectEvent extends Event {
        User getUser();
    }

    public static class User {
        private final String name;
        private final int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return this.name;
        }

        public int getAge() {
            return this.age;
        }
    }
}
