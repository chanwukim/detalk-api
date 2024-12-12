package net.detalk.api.repository;

import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.Member;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static net.detalk.jooq.Tables.MEMBER;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {
    private final DSLContext dsl;

    @Override
    public Member save(Member member) {
        return dsl.insertInto(MEMBER)
                .set(MEMBER.LOGIN_TYPE, member.getLoginType().getValue())
                .set(MEMBER.STATUS, member.getStatus().toString())
                .set(MEMBER.CREATED_AT, member.getCreatedAt())
                .set(MEMBER.UPDATED_AT, member.getUpdatedAt())
                .returning()
                .fetchOneInto(Member.class);
    }
}
