package net.detalk.api.role.repository.impl;

import static net.detalk.jooq.tables.JMemberRole.MEMBER_ROLE;
import static net.detalk.jooq.tables.JRole.ROLE;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.role.domain.Role;
import net.detalk.api.role.repository.RoleRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final DSLContext dsl;

    @Override
    public Optional<Role> findByCode(String code) {
        return dsl.selectFrom(ROLE)
            .where(ROLE.CODE.eq(code))
            .fetchOptionalInto(Role.class);
    }

    @Override
    public List<Role> findByCodes(Collection<String> codes) {
        return dsl.selectFrom(ROLE)
            .where(ROLE.CODE.in(codes))
            .fetchInto(Role.class);
    }

    @Override
    public List<Role> findRolesByMemberId(Long memberId) {
        return dsl.select(ROLE.fields())
            .from(ROLE)
            .join(MEMBER_ROLE)
            .on(ROLE.CODE.eq(MEMBER_ROLE.ROLE_CODE))
            .where(MEMBER_ROLE.MEMBER_ID.eq(memberId))
            .fetchInto(Role.class);
    }

    @Override
    public Role save(Role role) {
        return dsl.insertInto(ROLE)
            .set(ROLE.CODE, role.getCode())
            .set(ROLE.DESCRIPTION, role.getDescription())
            .returning()
            .fetchOneInto(Role.class);
    }

    @Override
    public void saveMemberRole(Long memberId, String roleCode) {
        dsl.insertInto(MEMBER_ROLE)
            .set(MEMBER_ROLE.MEMBER_ID, memberId)
            .set(MEMBER_ROLE.ROLE_CODE, roleCode)
            .onDuplicateKeyIgnore()  // 이미 존재하는 경우 무시
            .execute();
    }
}
