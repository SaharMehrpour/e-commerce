const cachedOrdersByUser = new Map();

export function getOrdersPageCache(userId) {
  return cachedOrdersByUser.get(userId) || {
    orders: [],
    hasLoaded: false,
  };
}

export function setOrdersPageCache(userId, orders) {
  cachedOrdersByUser.set(userId, {
    orders,
    hasLoaded: true,
  });
}

export function resetOrdersPageCache(userId) {
  if (userId) {
    cachedOrdersByUser.delete(userId);
  } else {
    cachedOrdersByUser.clear();
  }
}