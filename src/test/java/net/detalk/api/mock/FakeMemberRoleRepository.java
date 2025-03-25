package net.detalk.api.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.detalk.api.member.repository.MemberRoleRepository;

public class FakeMemberRoleRepository implements MemberRoleRepository {

    private final Map<Long, List<String>> memberRoles = new HashMap<>();

    @Override
    public List<String> findRolesByMemberId(Long memberId) {
        // memberId에 해당하는 역할 목록 반환. 없으면 빈 리스트 반환
        return memberRoles.getOrDefault(memberId, new ArrayList<>());
    }

}
