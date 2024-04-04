import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotBlank

@Entity
class Todo : PanacheEntity() {

    @NotBlank
    @Column(unique = true)
    lateinit var title: String

    var completed: Boolean = false

    @Column(name = "ordering")
    var order: Int? = null

    var url: String? = null

    companion object : PanacheCompanion<Todo> {

        fun findNotCompleted() = list("completed", false)

        fun findCompleted() = list("completed", true)

        fun deleteCompleted() = delete("completed", true)

    }
}