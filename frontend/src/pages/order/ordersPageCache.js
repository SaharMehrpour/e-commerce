let cachedOrders = [];
let cachedHasLoaded = false;

export function getOrdersPageCache() {
  return {
    orders: cachedOrders,
    hasLoaded: cachedHasLoaded,
  };
}

export function setOrdersPageCache(orders) {
  cachedOrders = orders;
  cachedHasLoaded = true;
}

export function resetOrdersPageCache() {
  cachedOrders = [];
  cachedHasLoaded = false;
}
