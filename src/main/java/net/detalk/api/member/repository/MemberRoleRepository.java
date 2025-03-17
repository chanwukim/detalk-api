package net.detalk.api.member.repository;

import java.util.List;

public interface MemberRoleRepository {
    List<String> findRolesByMemberId(Long memberId);
}
