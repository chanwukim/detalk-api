package net.detalk.api.repository;

import net.detalk.api.domain.Member;

public interface MemberRepository {
    Member save(Member member);
}
