'use client';
import { useFetchShopsList } from "@/products/hooks/client/use-fetch-shops-list";

const FilterBar = () => {
  const { data: shopList, isLoading, error } = useFetchShopsList();

  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Something went wrong!</div>;

  return (
    <div className="flex flex-col items-start rounded-lg text-blue-700 p-2 bg-white w-1/6 mt-4">
      <h2 className="font-bold text-2xl">Sklepy</h2>
      <ul>
        {shopList.map((shop, index) => (
          <li key={index}>{shop}</li>
        ))}
      </ul>
    </div>
  );
};

export { FilterBar };
