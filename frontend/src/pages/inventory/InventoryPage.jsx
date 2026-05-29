import { useState } from "react";
import { getInventory } from "../../api/inventory";
import { getInventoryPageCache, setInventoryPageCache } from "./inventoryPageCache";

function InventoryPage() {
  const initialCache = getInventoryPageCache();
  const [inventory, setInventory] = useState(initialCache.inventory);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [hasLoaded, setHasLoaded] = useState(initialCache.hasLoaded);

  async function loadInventory() {
    setError("");
    setIsLoading(true);
    setHasLoaded(true);

    try {
      const data = await getInventory();
      setInventoryPageCache(data);
      setInventory(data);
    } catch {
      setError("Could not load inventory. Check that the backend is running.");
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <section className="page-panel">
      <div className="d-flex justify-content-between align-items-start mb-3">
        <div>
          <h2 className="mb-1">All Inventory Items</h2>

          <p className="text-muted mb-0" style={{ fontSize: "0.9rem" }}>
            View and manage all inventory items
          </p>
        </div>

        {isLoading && (
          <div className="loading-overlay">
            <div className="spinner-box">
              <div className="spinner-border text-primary" />
              <div className="loading-text" role="status" aria-label="Loading Inventory">
                Loading inventory...
              </div>
            </div>
          </div>
        )}

        <button
          type="button"
          className="btn btn-primary btn-sm"
          onClick={loadInventory}
          disabled={isLoading}
        >
          {isLoading ? (
            <>
              <span
                className="spinner-border spinner-border-sm me-2"
                role="status"
              />
              Loading...
            </>
          ) : hasLoaded ? (
            <>
              <i className="bi bi-arrow-clockwise me-1"></i>
              Refresh
            </>
          ) : (
            <>
              <i className="bi bi-cloud-download me-1"></i>
              Load Inventory
            </>
          )}
        </button>
      </div>

      {!hasLoaded && (
        <p className="message">Click Load Inventory to fetch inventory items from the API.</p>
      )}

      {isLoading && <p className="message">Loading inventory...</p>}
      {error && <p className="message error">{error}</p>}

      {hasLoaded && !isLoading && !error && inventory.length === 0 && (
        <p className="message" role="status" aria-label="No inventory items yet">
          No inventory items yet.
        </p>
      )}

      {inventory.length > 0 && (
        <table className="table table-hover align-middle inventory-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Product ID</th>
              <th>Available Quantity</th>
              <th>Reserved Quantity</th>
            </tr>
          </thead>

          <tbody>
            {inventory.map((item) => (
              <tr key={item.id}>
                <td>{item.id}</td>
                <td className="user-cell">
                <span>{item.productId}</span>
                  <button
                    className="copy-btn"
                    onClick={() => navigator.clipboard.writeText(item.productId)}
                  >
                    <i className="bi bi-copy"></i>
                  </button>
                </td>
                <td>{item.availableQuantity}</td>
                <td>{item.reservedQuantity}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}

export default InventoryPage;
