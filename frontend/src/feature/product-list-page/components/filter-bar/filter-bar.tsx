"use client";
import { useFetchShopsList } from "@/products/hooks/client/use-fetch-shops-list";

const FilterBar = () => {
  const { data: shopList, isLoading, error } = useFetchShopsList();

  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Something went wrong!</div>;

  return (
    <div className="mt-4 flex flex-col items-start rounded-lg bg-white p-2 text-blue-700">
      <h2 className="text-2xl font-bold">Sklepy</h2>
      <ul>
        {shopList.map((shop, index) => (
          <li key={index}>{shop}</li>
        ))}
      </ul>
    </div>
  );
};

export { FilterBar };
