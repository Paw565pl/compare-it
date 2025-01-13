"use client";
import { useFetchShopsList } from "@/products/hooks/client/use-fetch-shops-list";

const FilterBar = () => {
  const { data: shopList, isLoading, error } = useFetchShopsList();

  if (isLoading) return <div className="text-blue-700">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;

  return (
    <div className="mt-4 flex flex-col items-start rounded-lg border border-gray-200 bg-white p-4 shadow-md">
      <h2 className="mb-4 text-2xl font-bold text-blue-700">Sklepy</h2>
      <ul className="space-y-2">
        {shopList?.map((shop, index) => (
          <li
            key={index}
            className="cursor-pointer rounded-lg px-4 py-2 transition-colors duration-200 hover:bg-blue-700 hover:text-white"
          >
            {shop}
          </li>
        ))}
      </ul>
    </div>
  );
};

export { FilterBar };
