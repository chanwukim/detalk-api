package net.detalk.api.role.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.detalk.api.role.domain.Role;

public interface RoleRepository {

    Optional<Role> findByCode(String code);

    List<Role> findByCodes(Collection<String> codes);

    List<Role> findRolesByMemberId(Long memberId);

    Role save(Role role);

    void saveMemberRole(Long memberId, String roleCode);

}
