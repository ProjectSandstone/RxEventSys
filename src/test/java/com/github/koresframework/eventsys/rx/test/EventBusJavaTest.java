package com.github.koresframework.eventsys.rx.test;

import com.github.koresframework.eventsys.event.Event;
import com.github.koresframework.eventsys.event.EventListener;
import com.github.koresframework.eventsys.event.EventManager;
import com.github.koresframework.eventsys.event.annotation.Name;
import com.github.koresframework.eventsys.gen.event.CommonEventGenerator;
import com.github.koresframework.eventsys.impl.CommonLogger;
import com.github.koresframework.eventsys.impl.DefaultEventManager;
import com.github.koresframework.eventsys.impl.PerChannelEventListenerRegistry;
import com.github.koresframework.eventsys.rx.Events;
import com.github.koresframework.eventsys.rx.impl.EventsImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;

public class EventBusJavaTest {
    @Test
    public void eventBusJavaTest() {
        Comparator<EventListener<?>> sorter = Comparator.comparing(EventListener::getPriority);
        CommonLogger logger = new CommonLogger();
        CommonEventGenerator commonEventGenerator = new CommonEventGenerator(logger);

        PerChannelEventListenerRegistry eventListenerRegistry = new PerChannelEventListenerRegistry(
                sorter,
                logger,
                commonEventGenerator
        );

        EventManager manager = new DefaultEventManager(eventListenerRegistry);

        Events events = new EventsImpl(eventListenerRegistry);

        MyEventFactory myEventFactory = (MyEventFactory) commonEventGenerator.createFactory(MyEventFactory.class).resolve();

        events.<ConnectEvent>observable(ConnectEvent.class)
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
