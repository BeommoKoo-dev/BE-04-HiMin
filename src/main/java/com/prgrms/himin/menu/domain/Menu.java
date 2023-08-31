package com.prgrms.himin.menu.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.prgrms.himin.order.domain.Order;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "menus")
public class Menu {

	private static final int MIN_PRICE = 0;

	private static final int MAX_NAME_LENGTH = 30;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name", nullable = false, length = 20)
	private String name;

	@Column(name = "price", nullable = false)
	private int price;

	@Column(name = "popularity", nullable = false, columnDefinition = "BIT(1)")
	private boolean popularity;

	@Enumerated
	@Column(name = "status", nullable = false)
	private MenuStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	@OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<MenuOptionGroup> menuOptionGroups = new ArrayList<>();

	@Builder
	public Menu(
		String name,
		int price,
		boolean popularity
	) {
		validateName(name);
		validatePrice(price);
		this.name = name;
		this.price = price;
		this.popularity = popularity;
		this.status = MenuStatus.unsellable;
	}

	public void addMenuOptionGroup(MenuOptionGroup menuOptionGroup) {
		if (!menuOptionGroups.contains(menuOptionGroup)) {
			this.menuOptionGroups.add(menuOptionGroup);
		}
	}

	public void removeMenuOptionGroup(MenuOptionGroup menuOptionGroup) {
		this.menuOptionGroups.remove(menuOptionGroup);
	}

	private void validatePrice(int price) {
		if (price < MIN_PRICE) {
			throw new RuntimeException("price는 음수가 되면 안됩니다.");
		}
	}

	private void validateName(String name) {
		if (name == null || name.length() > MAX_NAME_LENGTH) {
			throw new RuntimeException("잘못된 메뉴 이름 입니다.");
		}
	}
}
