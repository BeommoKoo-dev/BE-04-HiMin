package com.prgrms.himin.order.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prgrms.himin.global.error.exception.EntityNotFoundException;
import com.prgrms.himin.global.error.exception.ErrorCode;
import com.prgrms.himin.member.domain.Member;
import com.prgrms.himin.member.domain.MemberRepository;
import com.prgrms.himin.menu.domain.Menu;
import com.prgrms.himin.menu.domain.MenuOption;
import com.prgrms.himin.menu.domain.MenuOptionGroup;
import com.prgrms.himin.menu.domain.MenuOptionGroupRepository;
import com.prgrms.himin.menu.domain.MenuOptionRepository;
import com.prgrms.himin.menu.domain.MenuRepository;
import com.prgrms.himin.order.domain.Order;
import com.prgrms.himin.order.domain.OrderHistory;
import com.prgrms.himin.order.domain.OrderHistoryRepository;
import com.prgrms.himin.order.domain.OrderItem;
import com.prgrms.himin.order.domain.OrderRepository;
import com.prgrms.himin.order.domain.SelectedOption;
import com.prgrms.himin.order.dto.request.OrderCreateRequest;
import com.prgrms.himin.order.dto.request.SelectedMenuOptionRequest;
import com.prgrms.himin.order.dto.request.SelectedMenuRequest;
import com.prgrms.himin.order.dto.response.OrderResponse;
import com.prgrms.himin.shop.domain.Shop;
import com.prgrms.himin.shop.domain.ShopRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

	private final OrderRepository orderRepository;

	private final MenuRepository menuRepository;

	private final MemberRepository memberRepository;

	private final MenuOptionRepository menuOptionRepository;

	private final MenuOptionGroupRepository menuOptionGroupRepository;

	private final OrderHistoryRepository orderHistoryRepository;

	private final ShopRepository shopRepository;

	@Transactional
	public OrderResponse createOrder(OrderCreateRequest request) {
		Member member = memberRepository.findById(request.getMemberId())
			.orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

		Shop shop = shopRepository.findById(request.getShopId())
			.orElseThrow(() -> new RuntimeException("찾는 shop이 존재하지 않습니다."));

		Order order = Order.builder()
			.address(request.getAddress())
			.requirement(request.getRequirement())
			.shop(shop)
			.member(member)
			.build();

		List<SelectedMenuRequest> selectedMenus = request.getSelectedMenus();
		List<OrderItem> orderItems = extractOrderItems(selectedMenus);
		attachOrderItems(order, orderItems);

		OrderHistory orderHistory = OrderHistory.createOrderHistory(order);

		orderRepository.save(order);
		orderHistoryRepository.save(orderHistory);

		return OrderResponse.from(order);
	}

	public OrderResponse getOrder(Long orderId) {
		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new RuntimeException("찾는 Order가 존재하지 않습니다."));

		return OrderResponse.from(order);
	}

	private void attachOrderItems(
		Order order,
		List<OrderItem> orderItems
	) {
		for (OrderItem orderItem : orderItems) {
			orderItem.attachTo(order);
			order.addOrderPrice(orderItem.calculateOrderItemPrice());
		}
	}

	private List<OrderItem> extractOrderItems(List<SelectedMenuRequest> selectedMenus) {
		List<OrderItem> orderItems = new ArrayList<>();
		for (SelectedMenuRequest selectedMenu : selectedMenus) {
			Menu menu = menuRepository.findById(selectedMenu.getMenuId())
				.orElseThrow(() -> new RuntimeException("찾는 menu가 존재하지 않습니다."));

			List<SelectedMenuOptionRequest> selectedMenuOptions = selectedMenu.getSelectedMenuOptions();

			List<SelectedOption> selectedOptions = extractSelectedOptions(
				selectedMenu.getMenuId(),
				selectedMenuOptions
			);

			int quantity = selectedMenu.getQuantity();
			OrderItem orderItem = new OrderItem(
				menu,
				quantity
			);

			selectedOptions.forEach(selectedOption -> selectedOption.attachTo(orderItem));
			orderItems.add(orderItem);
		}

		return orderItems;
	}

	private List<SelectedOption> extractSelectedOptions(
		Long menuId,
		List<SelectedMenuOptionRequest> selectedMenuOptions
	) {
		List<SelectedOption> selectedOptions = new ArrayList<>();

		for (SelectedMenuOptionRequest selectedMenuOption : selectedMenuOptions) {
			Long menuOptionGroupId = selectedMenuOption.getMenuOptionGroupId();
			MenuOptionGroup menuOptionGroup = menuOptionGroupRepository.findById(menuOptionGroupId)
				.orElseThrow(() -> new RuntimeException("찾는 menuOptionGroup이 존재하지 않습니다."));

			checkMenuOptionGroup(menuId, menuOptionGroup);
			List<Long> menuOptionIds = selectedMenuOption.getSelectedMenuOptions();

			List<MenuOption> menuOptions = extractMenuOptions(menuOptionGroupId, menuOptionIds);
			List<SelectedOption> toAdd = SelectedOption.from(menuOptions);
			selectedOptions.addAll(toAdd);
		}

		return selectedOptions;
	}

	private List<MenuOption> extractMenuOptions(
		Long menuOptionGroupId,
		List<Long> menuOptionIds
	) {
		List<MenuOption> menuOptions = new ArrayList<>();
		for (Long menuOptionId : menuOptionIds) {
			MenuOption menuOption = menuOptionRepository.findById(menuOptionId)
				.orElseThrow(() -> new RuntimeException("찾는 menuOption이 존재하지 않습니다."));

			checkMenuOption(menuOptionGroupId, menuOption);
			menuOptions.add(menuOption);
		}

		return menuOptions;
	}

	private void checkMenuOptionGroup(Long menuId, MenuOptionGroup menuOptionGroup) {
		if (!menuId.equals(
			menuOptionGroup.getMenu().getId())) {
			throw new RuntimeException(new RuntimeException("잘못된 menuOptionGroup 요청입니다."));
		}
	}

	private void checkMenuOption(Long menuOptionGroupId, MenuOption menuOption) {
		if (!menuOptionGroupId.equals(
			menuOption.getMenuOptionGroup().getId())) {
			throw new RuntimeException(new RuntimeException("잘못된 menuOption 요청입니다."));
		}
	}
}
