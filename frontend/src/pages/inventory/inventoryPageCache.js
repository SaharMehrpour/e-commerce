let cachedInventory = [];
let cachedHasLoaded = false;

export function getInventoryPageCache() {
  return {
    inventory: cachedInventory,
    hasLoaded: cachedHasLoaded,
  };
}

export function setInventoryPageCache(inventory) {
  cachedInventory = inventory;
  cachedHasLoaded = true;
}

export function resetInventoryPageCache() {
  cachedInventory = [];
  cachedHasLoaded = false;
}
