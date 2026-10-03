package com.example.playlistmaker.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MutableLiveData<T>() {

    private val pending = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) { data ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(data)
            }
        }
    }

    fun setEvent(newValue: T) {
        pending.set(true)
        value = newValue
    }
}
