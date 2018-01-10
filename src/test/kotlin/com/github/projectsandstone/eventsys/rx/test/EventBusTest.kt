/*
 *      RxEventSys - Java ReactiveX EventSys implementation
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2018 ProjectSandstone <https://github.com/ProjectSandstone/RxEventSys>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.projectsandstone.eventsys.rx.test

import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.projectsandstone.eventsys.ap.Factory
import com.github.projectsandstone.eventsys.event.Event
import com.github.projectsandstone.eventsys.event.annotation.Name
import com.github.projectsandstone.eventsys.event.annotation.TypeParam
import com.github.projectsandstone.eventsys.impl.DefaultEventManager
import com.github.projectsandstone.eventsys.rx.Events
import com.github.projectsandstone.eventsys.rx.impl.EventsImpl
import com.github.projectsandstone.eventsys.rx.impl.ObservableMethodInterfaceGeneratorImpl
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Test

class EventBusTest {

    @Test
    fun testObservableBus() {
        val manager = DefaultEventManager()
        val events: Events = EventsImpl(manager)
        val gen = ObservableMethodInterfaceGeneratorImpl(events)
        val factory = manager.eventGenerator.createFactory(EvtFactory::class.java)
        val myEvents = gen.create(MyEvents::class.java)

        val list = mutableListOf<Int>()

        events.observable(MyEvent::class.java)
                .map { it.amount }
                .filter { it > 9 }
                .subscribe({ list += it }, { it.printStackTrace() })


        manager.dispatch(factory.createMyEvent(5), this)
        manager.dispatch(factory.createMyEvent(9), this)
        manager.dispatch(factory.createMyEvent(10), this)
        manager.dispatch(factory.createMyEvent(19), this)

        Assert.assertEquals(listOf(10, 19), list)

        myEvents.myEvent()
                .map { it.amount }
                .filter { it > 9 }
                .subscribe({ list += it }, { it.printStackTrace() })

        manager.dispatch(factory.createMyEvent(391), this)
        manager.dispatch(factory.createMyEvent(3), this)

        Assert.assertEquals(listOf(10, 19, 391, 391), list)
    }

    interface MyEvents {
        fun myEvent(): Observable<MyEvent>
        fun <T> myEvent2(@TypeParam type: TypeInfo<T>): Observable<MyGenericEvent<T>>
    }

    interface MyGenericEvent<T> : Event {
        val value: T
    }

    interface EvtFactory {
        fun createMyEvent(@Name("amount") amount: Int): MyEvent
    }

    @Factory("com.github.projectsandstone.eventsys.rx.EvtFactory")
    interface MyEvent : Event {
        val amount: Int
    }

}