package com.prgrms.himin.member.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prgrms.himin.member.domain.Address;
import com.prgrms.himin.member.domain.Member;
import com.prgrms.himin.member.domain.MemberRepository;
import com.prgrms.himin.member.dto.request.MemberCreateRequest;
import com.prgrms.himin.member.dto.request.MemberLoginRequest;
import com.prgrms.himin.member.dto.response.MemberCreateResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;

	@Transactional
	public MemberCreateResponse createMember(MemberCreateRequest request) {
		Member member = request.toEntity();
		Address address = new Address(
			request.getAddressAlias(),
			request.getAddress()
		);
		address.attachTo(member);
		Member savedMember = memberRepository.save(member);
		return MemberCreateResponse.from(savedMember);
	}

	public void login(MemberLoginRequest request) {
		if (!memberRepository.existsMemberByLoginIdAndPassword(
			request.getLoginId(),
			request.getPassword()
		)) {
			throw new RuntimeException("로그인할 정보가 일치하지 않습니다!");
		}
	}
}
