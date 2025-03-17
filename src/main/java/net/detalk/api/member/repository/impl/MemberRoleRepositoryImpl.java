package net.detalk.api.member.repository.impl;

import static net.detalk.jooq.tables.JMemberRole.MEMBER_ROLE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.detalk.api.member.repository.MemberRoleRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MemberRoleRepositoryImpl implements MemberRoleRepository {

    private final DSLContext dsl;

    @Override
    public List<String> findRolesByMemberId(Long memberId) {
        return dsl.select(MEMBER_ROLE.ROLE_CODE)
            .from(MEMBER_ROLE)
            .where(MEMBER_ROLE.MEMBER_ID.eq(memberId))
            .fetchInto(String.class);
    }
}
