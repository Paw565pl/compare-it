"use client";
import { useFetchShopsList } from "@/products/hooks/client/use-fetch-shops-list";
import { parseAsFloat, parseAsString, useQueryStates } from "nuqs";
import { useState } from "react";

const FilterBar = () => {
  const { data: shopList } = useFetchShopsList();
  const [, setProductFilters] = useQueryStates({
    category: parseAsString,
    minPrice: parseAsFloat,
    maxPrice: parseAsFloat,
    shop: parseAsString,
    name: parseAsString,
  });

  const [tempProductFilters, setTempProductFilters] = useState({
    minPrice: null,
    maxPrice: null,
    shop: ["Morele.net", "RTV Euro AGD"],
  });

  const handlePriceChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;

    setTempProductFilters((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleShopChange = (e: React.ChangeEvent<HTMLInputElement>) => {
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
      <h2 className="mb-1 ml-4 mt-6 text-2xl font-bold text-secondary sm:ml-0">
        Filtry
      </h2>
      <div className="mb-4 mt-2 flex flex-col items-start border border-background bg-white">
        <h3 className="mb-2 pl-4 pt-2 text-xl font-bold text-secondary">
          Sklepy
        </h3>
        <ul className="w-full">
          {shopList
            ?.filter((shop) => shop !== "Media Expert")
            .map((shop, index) => (
              <li
                key={index}
                className="cursor-pointer px-4 py-2 transition-colors duration-200 hover:bg-hover hover:text-white sm:text-lg"
              >
                <label className="flex items-center space-x-2 hover:cursor-pointer">
                  <input
                    type="checkbox"
                    name="shop"
                    value={shop}
                    checked={tempProductFilters.shop.includes(shop)}
                    onChange={handleShopChange}
                    className="cursor-pointer"
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
              className="w-full bg-background p-2 text-sm focus:outline-none"
            />
            <input
              type="text"
              name="maxPrice"
              onChange={handlePriceChange}
              id="maxPrice"
              placeholder="do"
              className="w-full bg-background p-2 text-sm focus:outline-none"
            />
          </div>

          <button
            onClick={applyFilters}
            className="bg-secondary px-4 py-2 text-white hover:bg-hover"
          >
            FILTRUJ
          </button>
        </div>
      </div>
    </>
  );
};

export { FilterBar };
