"use client";
import { useFetchShopsList } from "@/products/hooks/client/use-fetch-shops-list";
import { useQueryStates } from "nuqs";
import { useState } from "react";

const FilterBar = () => {
  const { data: shopList, isLoading, error } = useFetchShopsList();
  const [productFilters, setProductFilters] = useQueryStates({
    category: "Karty graficzne",
    size: 5,
    minPrice: 1900,
    maxPrice: 2000,
    shop: ["Morele.net", "RTV Euro AGD", "Media Expert"],
  });

  const [tempProductFilters, setTempProductFilters] = useState({
    category: "Karty graficzne",
    size: 5,
    minPrice: 1900,
    maxPrice: 2000,
    shop: ["Morele.net", "RTV Euro AGD", "Media Expert"],
  });

  if (isLoading) return <div className="text-secondary">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;

  const handlePriceChange = (e) => {
    const { name, value } = e.target;

    setTempProductFilters((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleShopChange = (e) => {
    const { value, checked } = e.target;

    setTempProductFilters((prev) => {
      const updatedShops = checked
        ? [...prev.shop, value]
        : prev.shop.filter((shop) => shop !== value);

      return {
        ...prev,
        shop: updatedShops,
      };
    });
  };

  const applyFilters = () => {
    const filtersWithShops = {
      ...tempProductFilters,
      shop: tempProductFilters.shop.join(","),
    };

    setProductFilters((prevFilters) => ({
      ...prevFilters,
      ...filtersWithShops,
    }));
  };

  return (
    <>
      <h2 className="mt-6 text-2xl font-bold text-secondary">Filtry</h2>
      <div className="mt-2 flex flex-col items-start border border-background bg-white">
        <h3 className="mb-2 pl-4 pt-2 text-xl font-bold text-secondary">
          Sklepy
        </h3>
        <ul className="w-full">
          {shopList?.map((shop, index) => (
            <li
              key={index}
              className="cursor-pointer px-4 py-2 transition-colors duration-200 hover:bg-secondary hover:text-white"
            >
              <label className="flex items-center space-x-2">
                <input
                  type="checkbox"
                  name="shop"
                  value={shop}
                  checked={tempProductFilters.shop.includes(shop)}
                  onChange={handleShopChange}
                />
                <span>{shop}</span>
              </label>
            </li>
          ))}
        </ul>
        <h3 className="pl-4 pt-2 text-xl font-bold text-secondary">Cena</h3>
        <div className="flex w-full flex-col justify-start">
          <div className="mb-4 w-full space-y-1 p-4">
            <input
              type="text"
              name="minPrice"
              onChange={handlePriceChange}
              id="minPrice"
              placeholder="od"
              value={tempProductFilters.minPrice}
              className="w-full bg-background p-2 text-sm focus:outline-none"
            />
            <input
              type="text"
              name="maxPrice"
              onChange={handlePriceChange}
              id="maxPrice"
              placeholder="do"
              value={tempProductFilters.maxPrice}
              className="w-full bg-background p-2 text-sm focus:outline-none"
            />
          </div>

          <button
            onClick={applyFilters}
            className="bg-secondary px-4 py-2 text-white"
          >
            FILTRUJ
          </button>
        </div>
      </div>
    </>
  );
};

export { FilterBar };
