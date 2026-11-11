package net.moonbaseone.vera.shared.appointments

/**
 * Local pocket-guide persistence. Default is in-process; Android can override
 * with a durable store later without changing ViewModel call sites.
 */
interface PocketGuideStore {
    fun list(): List<PocketGuide>
    fun get(appointmentId: String): PocketGuide?
    fun save(guide: PocketGuide)
    fun delete(appointmentId: String)
}

class InMemoryPocketGuideStore : PocketGuideStore {
    private val guides = linkedMapOf<String, PocketGuide>()

    override fun list(): List<PocketGuide> = guides.values.toList()

    override fun get(appointmentId: String): PocketGuide? = guides[appointmentId]

    override fun save(guide: PocketGuide) {
        guides[guide.appointmentId] = guide
    }

    override fun delete(appointmentId: String) {
        guides.remove(appointmentId)
    }
}
