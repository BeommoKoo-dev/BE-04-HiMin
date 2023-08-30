package com.prgrms.himin.member.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findMemberByLoginIdAndPassword(String loginId, String password);
}
