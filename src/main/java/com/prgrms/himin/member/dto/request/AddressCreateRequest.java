package com.prgrms.himin.member.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.prgrms.himin.member.domain.Address;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressCreateRequest {

	@Size(max = 10, message = "주소별칭은 최대 10글자 입니다.")
	@NotBlank(message = "주소별칭이 비어있으면 안됩니다.")
	private final String addressAlias;

	@Size(max = 50, message = "주소는 최대 50글자 입니다.")
	@NotBlank(message = "주소는 비어있으면 안됩니다.")
	private final String address;

	public Address toEntity() {
		return new Address(
			addressAlias,
			address
		);
	}
}