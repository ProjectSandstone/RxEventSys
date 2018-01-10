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
package com.github.projectsandstone.eventsys.rx.impl

import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.projectsandstone.eventsys.event.Event
import com.github.projectsandstone.eventsys.event.EventListener
import com.github.projectsandstone.eventsys.event.EventManager
import com.github.projectsandstone.eventsys.rx.Events
import io.reactivex.Observable
import io.reactivex.subjects.Subject

class EventsImpl(val eventManager: EventManager) : Events {

    private val observables = mutableMapOf<TypeInfo<*>, Observable<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Event> observable(eventType: TypeInfo<T>): Observable<T> =
            this.observables.computeIfAbsent(eventType) {
                EventSubjectImpl<T>().also { registerListener(eventType, it) }
            } as Observable<T>

    private fun <T: Event> registerListener(eventType: TypeInfo<T>, subject: Subject<T>) {
        this.eventManager.registerListener(this, eventType, EvtListener(subject))
    }

    private class EvtListener<T: Event>(val subject: Subject<T>) : EventListener<T> {
        override fun onEvent(event: T, owner: Any) {
            this.subject.onNext(event)
        }
    }
}