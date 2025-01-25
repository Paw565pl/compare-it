"use client";
import { useFetchShopsList } from "@/products/hooks/client/use-fetch-shops-list";
import { useQueryStates } from "nuqs";
import { useState } from "react";

const FilterBar = () => {
  const { data: shopList, isLoading, error } = useFetchShopsList();
  const [_, setProductFilters] = useQueryStates({
    shop: "Morele.net",
    minPrice: 1,
    maxPrice: 100000,
  });

  const [tempProductFilters, setTempProductFilters] = useState({
    shop: "Morele.net",
    minPrice: 1,
    maxPrice: 100000,
  });

  if (isLoading) return <div className="text-blue-700">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;

  const handlePriceChange = (e) => {
    const { name, value } = e.target;

    setTempProductFilters((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  return (
    <>
      <h2 className="mt-6 text-2xl font-bold text-blue-700">Filtry</h2>
      <div className="mt-2 flex flex-col items-start rounded-b-lg border border-gray-100 bg-white">
        <h3 className="mb-2 pl-4 pt-2 text-xl font-bold text-blue-700">
          Sklepy
        </h3>
        <ul className="w-full space-y-2">
          {shopList?.map((shop, index) => (
            <li
              key={index}
              className="cursor-pointer rounded-lg px-4 py-2 transition-colors duration-200 hover:bg-blue-700 hover:text-white"
            >
              <button onClick={() => setProductFilters({ shop: shop })}>
                {shop}
              </button>
            </li>
          ))}
        </ul>
        <h3 className="pl-4 pt-2 text-xl font-bold text-blue-700">Cena</h3>
        <div className="flex w-full flex-col">
          <div className="space-y-1 p-4">
            <input
              type="text"
              name="minPrice"
              onChange={handlePriceChange}
              id="minPrice"
              placeholder="Od"
              className="rounded-lg bg-gray-100 px-2 py-1"
            />
            <input
              type="text"
              name="maxPrice"
              onChange={handlePriceChange}
              id="maxPrice"
              placeholder="Do"
              className="mb-8 rounded-lg bg-gray-100 px-2 py-1"
            />
          </div>

          <button
            onClick={() => setProductFilters(tempProductFilters)}
            className="rounded-lg bg-blue-700 px-4 py-2 text-white"
          >
            FILTRUJ
          </button>
        </div>
      </div>
    </>
  );
};

export { FilterBar };
