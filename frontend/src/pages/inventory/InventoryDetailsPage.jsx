import { useState } from "react";
import { getInventoryItemById, updateInventoryItem } from "../../api/inventory";

function InventoryDetailsPage() {
  const [inventoryItemId, setInventoryItemId] = useState("");
  const [inventoryItem, setInventoryItem] = useState(null);
  const [inventoryItemFields, setInventoryItemFields] = useState(null);
  const [validatedInventoryItem, setValidatedInventoryItem] = useState(null);

  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isEditing, setIsEditing] = useState(false);

  async function handleFindInventoryItem(event) {
    event.preventDefault();
    setError("");
    setInventoryItem(null);
    setInventoryItemFields(null);
    setValidatedInventoryItem(null);
    setIsEditing(false);

    try {
      setIsLoading(true);
      const data = await getInventoryItemById(inventoryItemId.trim());
      setInventoryItem(data);
    } catch (err) {
      console.error("Failed to fetch inventory item", { err });
      setError(
        err?.status === 404
          ? "No inventory item found for that ID."
          : "Backend is not reachable. Please try again later."
      );
    } finally {
      setIsLoading(false);
    }
  }

  async function handleUpdate() {
    try {
      setIsLoading(true);

      const data = await updateInventoryItem(inventoryItemId.trim(), validatedInventoryItem);

      setInventoryItem(data);
      setInventoryItemFields(null);
      setIsEditing(false);
    } catch (err) {
      console.error("Failed to update inventory item", { err });
      setError("Failed to update the inventory item.");
    } finally {
      setValidatedInventoryItem(null);
      setIsLoading(false);
    }
  }

  function handleChange(field, value) {
    setInventoryItemFields((prev) => ({
      ...(prev || inventoryItem),
      [field]: value,
    }));

    setValidatedInventoryItem((prev) => {
      const nextState = { ...prev };
      if (inventoryItem && inventoryItem[field] === value) {
        delete nextState[field];
      } else {
        nextState[field] = value;
      }
      return nextState;
    });
  }

  const displayInventoryItem = inventoryItemFields || inventoryItem;
  const isSaveDisabled = !validatedInventoryItem || Object.keys(validatedInventoryItem).length === 0 || isLoading;


  function renderSearch() {
    return (
      <form onSubmit={handleFindInventoryItem} className="mb-4">
        <div className="input-group">
          <input
            type="text"
            className="form-control"
            value={inventoryItemId}
            onChange={(e) => setInventoryItemId(e.target.value)}
            placeholder="Enter product ID"
            required
          />

          <button
            type="submit"
            className="btn btn-primary"
            disabled={isLoading}
          >
            {isLoading ? "Searching..." : "Get inventory item"}
          </button>
        </div>
      </form>
    );
  }

  function renderIDField() {
    return (
      <div className="binventoryItem rounded p-2 mb-3 bg-light">
        <div className="inventoryItem-field-label mb-1">
          Inventory Item ID
        </div>

        <div
          className="text-truncate"
          style={{
            fontSize: "0.9rem",
            fontFamily: "monospace",
          }}
          title={inventoryItem.id}
        >
          {inventoryItem.id}
        </div>
      </div>
    );
  }

  function renderProductField() {
    return (
      <div className="col-md-4">
        <div className="inventoryItem-field compact-field">
          <span className="inventoryItem-field-label">
            Product
          </span>
          <span>{inventoryItem.productId}</span>
        </div>
      </div>
    );
  }

  function renderQuantityField(field, label) {
    return (
      <div className="col-md-4">
        <div className="inventoryItem-field compact-field">
          <span className="inventoryItem-field-label">
            {label}
          </span>

          {isEditing ? (
            <input
              type="number"
              min="1"
              className="form-control form-control-sm"
              value={displayInventoryItem?.[field] || 1}
              onChange={(e) => handleChange(field, Number(e.target.value))}
            />
          ) : (
            <span>{inventoryItem[field]}</span>
          )}
        </div>
      </div>
    );
  }

  return (
    <section className="page-panel">
      <div className="mb-4">
        <h2 className="mb-1">Inventory Item Lookup</h2>

        <p className="section-subtitle mb-0">
          Search for an inventory item by ID
        </p>
      </div>

      {isLoading && (
        <div className="loading-overlay">
          <div className="spinner-box">
            <div className="spinner-binventoryItem text-primary" role="status" />
            <div className="loading-text">
              Loading inventoryItem...
            </div>
          </div>
        </div>
      )}

      {/* Search */}
      {renderSearch()}

      {/* Error */}
      {error && (
        <div className="alert alert-danger py-2">
          {error}
        </div>
      )}

      {/* InventoryItem Card (same style as CreateInventoryItemPage result) */}
      {inventoryItem && (
        <div className="alert alert-success binventoryItem-0 shadow-sm mt-4">

          {/* ID */}
          {renderIDField()}

          {/* Compact fields */}
          <div className="row g-2">
            {/* Product */}
            {renderProductField()}

            {/* Available Quantity */}
            {renderQuantityField("availableQuantity", "Available Quantity")}

            {/* Reserved Quantity */}
            {renderQuantityField("reservedQuantity", "Reserved Quantity")}
          </div>

          {/* Actions */}
          <div className="mt-4 d-flex justify-content-end gap-2">
            {!isEditing ? (
              <>
                <button
                  className="btn btn-primary btn-sm"
                  type="button"
                  onClick={() => setIsEditing(true)}
                >
                  Update inventory item
                </button>
              </>
            ) : (
              <>
                <button
                  className="btn btn-light binventoryItem btn-sm"
                  type="button"
                  onClick={() => {
                    setIsEditing(false);
                    setInventoryItemFields(null);
                    setValidatedInventoryItem(null);
                  }}
                >
                  Discard
                </button>

                <button
                  className="btn btn-success btn-sm"
                  type="button"
                  onClick={handleUpdate}
                  disabled={isSaveDisabled}
                >
                  Save Changes
                </button>
              </>
            )}
          </div>
        </div>
      )}
    </section>
  );
}

export default InventoryDetailsPage;
