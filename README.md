# RxEventSys

Implementation of [RxJava](https://github.com/ReactiveX/RxJava) `Observer` as an [EventSys](https://github.com/ProjectSandstone/EventSys) Event Listener.

# Usage example:

```java
EventManager manager = ...;
Events events = new EventsImpl(manager);
events.observable(ConnectEvent.class)
        .map(ConnectEvent::getUser)
        .filter(user -> user.getAge() >= 18)
        .subscribe(user -> ...);
```

# @ObservableEvent

Works in the same way as [EventSys Factory](https://github.com/ProjectSandstone/EventSys/wiki/Factory-annotation), but generates a stub interface that return `Observable<T>` event handlers (this means that returned observable observes handling of annotated event). The `value` of *@ObservableEvent* cannot be the same as of the *@Factory* (obvious reasons).

To generate implementation of generated stub observable event handler interface, you can use `ObservableMethodInterfaceGeneratorImpl` (default implementation of `ObservableMethodInterfaceGenerator`).

**I hate these long names... I don't know why I gave this f- long name for them.**

Example:

```kotlin
@ObservableEvent("com.github.projectsandstone.eventsys.rx.example.ExampleEvents")
interface BuyEvent : Event {
    val user: User
    val amount: Int
}

class ObserveBuyEventExample {
    fun example() {
        val events = ...
        val generator = ObservableMethodInterfaceGeneratorImpl(events)
        val exampleEvents = generator.create(ExampleEvents::class.java) // ExampleEvents = Generated stub interface
        exampleEvents.buyEvent()
            .map { it.user }
            .filter { it.age >= 18 }
            .subscribe { ... }
    }
}
```