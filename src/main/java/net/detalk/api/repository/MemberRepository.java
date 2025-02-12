package net.detalk.api.repository;

import net.detalk.api.domain.Member;

import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Member update(Member member);
}
