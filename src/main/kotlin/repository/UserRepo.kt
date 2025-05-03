package com.company.repository

import com.company.config.toFlux
import com.company.config.toMono
import com.company.jooq.tables.records.UsersRecord
import com.company.jooq.tables.references.USERS
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.koin.core.annotation.Single

@Single
class UserRepo(
) {
    /**
     * Inserts a new user with the given name and returns a list of users
     * whose names match a specific pattern.
     */
    suspend fun insertAndFindUsers(name: String, context: DSLContext): List<UsersRecord> {
        // Insert new user
        context.insertInto(USERS)
            .set(USERS.NAME, name)
            .toMono()
            .awaitFirstOrNull()

        // Select users matching pattern
        return context.selectFrom(USERS)
            .where(DSL.lower(USERS.NAME).like("%g%d%w%"))
            .orderBy(USERS.CREATED.desc())
            .limit(5)
            .toFlux()
            .collectList()
            .awaitSingle()

    }
}
