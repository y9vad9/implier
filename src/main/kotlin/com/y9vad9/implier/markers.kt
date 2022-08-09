package com.y9vad9.implier

interface GenericMarker<T>
interface Mutable<T> : GenericMarker<T>
interface Immutable<T> : GenericMarker<T>
interface Dto<T> : GenericMarker<T>
