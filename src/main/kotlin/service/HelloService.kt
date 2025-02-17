package com.company.service

import com.company.config.Database
import com.company.jooq.tables.references.USERS
import com.company.config.toFlux
import com.company.config.toMono
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.impl.DSL
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.ConcurrentHashMap

class HelloService : KoinComponent {
    private val database: Database by inject()

    val map = ConcurrentHashMap<String, String>()

    suspend fun hello(): List<DTO> {
        val charset = ('a'..'z').toList()
        val name = (1..128)
            .map { charset.random() }
            .joinToString("")

        if (map.containsKey(name)) {
            println("collision")
            return listOf()
        }
        map.put(name, "1")

        val list = database.transactionalJooq { context ->

            context.insertInto(USERS)
                .set(USERS.NAME, name)
                .toMono()
                .awaitFirstOrNull()

            context.selectFrom(USERS)
                .where(DSL.lower(USERS.NAME).like("%g%d%w%"))
                .orderBy(USERS.CREATED.desc())
                .limit(5)
                .toFlux()
                .collectList()
                .awaitSingle()
        }
        return list.map {
            DTO(it.id!!, it.name)
        }
    }

    data class DTO(
        val id: Long,
        val name: String,
    )
}