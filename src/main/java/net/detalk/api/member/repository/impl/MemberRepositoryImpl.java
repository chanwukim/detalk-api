package net.detalk.api.member.repository.impl;

import lombok.RequiredArgsConstructor;
import net.detalk.api.member.domain.Member;

import net.detalk.api.member.repository.MemberRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static net.detalk.jooq.Tables.*;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {
    private final DSLContext dsl;

    @Override
    public Member save(Member member) {
        return dsl.insertInto(MEMBER)
            .set(MEMBER.LOGIN_TYPE, member.getLoginType())
            .set(MEMBER.STATUS, member.getStatus())
            .set(MEMBER.CREATED_AT, member.getCreatedAt())
            .set(MEMBER.UPDATED_AT, member.getUpdatedAt())
            .returning()
            .fetchOneInto(Member.class);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return dsl.selectFrom(MEMBER)
            .where(MEMBER.ID.eq(id))
            .fetchOptionalInto(Member.class);
    }

    @Override
    public Member update(Member member) {
        return dsl.update(MEMBER)
            .set(MEMBER.STATUS, member.getStatus())
            .set(MEMBER.UPDATED_AT, member.getUpdatedAt())
            .where(MEMBER.ID.eq(member.getId()))
            .returning()
            .fetchOneInto(Member.class);
    }
}
