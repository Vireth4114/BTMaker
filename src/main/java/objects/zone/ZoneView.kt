package objects.zone

import objects.GameObjectView

abstract class ZoneView<T: Zone>(model: T) : GameObjectView<T>(model)