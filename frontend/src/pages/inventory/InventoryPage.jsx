import { useState, useContext } from "react";
import { getInventory, updateStock } from "../../api/inventory";
import { getInventoryPageCache, setInventoryPageCache } from "./inventoryPageCache";
import { AuthContext } from "../../auth/AuthContext";

function InventoryPage() {
  const initialCache = getInventoryPageCache();
  const { keycloak, authenticated } = useContext(AuthContext);

  const [inventory, setInventory] = useState(initialCache.inventory);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [hasLoaded, setHasLoaded] = useState(initialCache.hasLoaded);

  const [productId, setProductId] = useState("");
  const [newQuantity, setNewQuantity] = useState("");

  const [editingProductId, setEditingProductId] = useState(null);
  const [editQuantity, setEditQuantity] = useState("");

  async function loadInventory() {
    setError("");
    setIsLoading(true);
    setHasLoaded(true);

    try {
      const data = await getInventory(keycloak);
      setInventoryPageCache(data);
      setInventory(data);
    } catch (error) {
      setError(error.message);
    } finally {
      setIsLoading(false);
    }
  }

  async function handleAddItem(event) {
    event.preventDefault();
    setError("");
    setSuccess("");

    const quantity = Number(newQuantity);
    const trimmedProductId = productId.trim();

    if (!trimmedProductId || newQuantity === "" ||
      !Number.isInteger(quantity) || quantity < 1) {
      setError("Enter a product ID and a non-negative whole-number quantity.");
      return;
    }

    setIsSaving(true);

    try {
      await updateStock(keycloak, trimmedProductId, quantity);

      setProductId("");
      setNewQuantity("");
      setSuccess("Inventory item updated successfully.");

      await loadInventory();
    } catch (error) {
      setError(error.message);
    } finally {
      setIsSaving(false);
    }
  }

  function startEditing(item) {
    setEditingProductId(item.productId);
    setEditQuantity(String(item.availableQuantity));
    setError("");
    setSuccess("");
  }

  async function handleUpdateQuantity(event) {
    event.preventDefault();
    setError("");
    setSuccess("");

    const quantity = Number(editQuantity);

    if (
      editQuantity === "" ||
      !Number.isInteger(quantity) ||
      quantity < 1
    ) {
      setError("Enter a non-negative whole-number quantity.");
      return;
    }

    setIsSaving(true);

    try {
      await updateStock(keycloak, editingProductId, quantity);

      setEditingProductId(null);
      setEditQuantity("");
      setSuccess("Quantity updated successfully.");

      await loadInventory();
    } catch (error) {
      setError(error.message);
    } finally {
      setIsSaving(false);
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

        <button
          type="button"
          className="btn btn-primary btn-sm"
          onClick={loadInventory}
          disabled={isLoading || isSaving}
        >
          {isLoading ? "Loading..." : hasLoaded ? "Refresh" : "Load Inventory"}
        </button>
      </div>

      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {success && (
        <div className="alert alert-success" role="status">
          {success}
        </div>
      )}

      {!authenticated && (
        <p className="message">Please log in to manage inventory.</p>
      )}

      {authenticated && hasLoaded && (
        <>
          {/* Add a new inventory item */}
          <div className="card mb-4">
            <div className="card-body">
              <h5 className="card-title">Add Inventory Item</h5>

              <form onSubmit={handleAddItem}>
                <div className="row g-3 align-items-end">
                  <div className="col-md-5">
                    <label htmlFor="productId" className="form-label">
                      Product ID
                    </label>
                    <input
                      id="productId"
                      className="form-control"
                      value={productId}
                      onChange={(event) => setProductId(event.target.value)}
                      placeholder="Enter product ID"
                      required
                      disabled={isSaving}
                    />
                  </div>

                  <div className="col-md-3">
                    <label htmlFor="newQuantity" className="form-label">
                      Initial Quantity
                    </label>
                    <input
                      id="newQuantity"
                      type="number"
                      min="1"
                      step="1"
                      className="form-control"
                      value={newQuantity}
                      onChange={(event) => setNewQuantity(event.target.value)}
                      placeholder="0"
                      required
                      disabled={isSaving}
                    />
                  </div>

                  <div className="col-md-4">
                    <button
                      type="submit"
                      className="btn btn-success"
                      disabled={isSaving}
                    >
                      Add Item
                    </button>
                  </div>
                </div>
              </form>
            </div>
          </div>

          {/* Existing inventory */}
          <h5 className="mb-3">Inventory Items</h5>

          {isLoading ? (
            <p className="message">Loading inventory...</p>
          ) : inventory.length === 0 ? (
            <p className="message">No inventory items yet.</p>
          ) : (
            <table className="table table-hover align-middle inventory-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Product ID</th>
                  <th>Available Quantity</th>
                  <th>Reserved Quantity</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {inventory.map((item) => (
                  <tr key={item.id}>
                    <td>{item.id}</td>

                    <td className="user-cell">
                      <span>{item.productId}</span>
                      <button
                        type="button"
                        className="copy-btn"
                        aria-label="Copy product ID"
                        onClick={() =>
                          navigator.clipboard.writeText(item.productId)
                        }
                      >
                        <i className="bi bi-copy"></i>
                      </button>
                    </td>

                    <td>
                      {editingProductId === item.productId ? (
                        <form
                          onSubmit={handleUpdateQuantity}
                          className="d-flex gap-2"
                        >
                          <input
                            type="number"
                            min="0"
                            step="1"
                            className="form-control"
                            style={{ width: "110px" }}
                            value={editQuantity}
                            onChange={(event) =>
                              setEditQuantity(event.target.value)
                            }
                            aria-label="New available quantity"
                            required
                            disabled={isSaving}
                            autoFocus
                          />

                          <button
                            type="submit"
                            className="btn btn-success btn-sm"
                            disabled={isSaving}
                          >
                            Save
                          </button>

                          <button
                            type="button"
                            className="btn btn-secondary btn-sm"
                            onClick={() => setEditingProductId(null)}
                            disabled={isSaving}
                          >
                            Cancel
                          </button>
                        </form>
                      ) : (
                        item.availableQuantity
                      )}
                    </td>

                    <td>{item.reservedQuantity}</td>

                    <td>
                      {editingProductId !== item.productId && (
                        <button
                          type="button"
                          className="btn btn-outline-primary btn-sm"
                          onClick={() => startEditing(item)}
                          disabled={isSaving}
                        >
                          <i className="bi bi-pencil me-1"></i>
                          Edit Quantity
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </section>
  );
}

export default InventoryPage;
