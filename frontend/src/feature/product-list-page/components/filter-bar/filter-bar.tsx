"use client";
import { useFetchShopsList } from "@/products/hooks/client/use-fetch-shops-list";
import { useQueryStates } from "nuqs";

const FilterBar = () => {
  const { data: shopList, isLoading, error } = useFetchShopsList();
  const [priceFilters, setPriceFilters] = useQueryStates({
    shop: "Morele.net",
    minPrice: 1,
    maxPrice: 10000,
  });

  if (isLoading) return <div className="text-blue-700">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;

  const handlePriceChange = (e) => {
    const { name, value } = e.target;

    setPriceFilters((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  return (
    <>
      <div className="mt-4 flex flex-col items-start rounded-lg border border-gray-200 bg-white p-4 shadow-md">
        <h2 className="mb-4 text-2xl font-bold text-blue-700">Sklepy</h2>
        <ul className="space-y-2">
          {shopList?.map((shop, index) => (
            <li
              key={index}
              className="cursor-pointer rounded-lg px-4 py-2 transition-colors duration-200 hover:bg-blue-700 hover:text-white"
            >
              <button onClick={() => setPriceFilters({ shop: shop })}>
                {shop}
              </button>
            </li>
          ))}
        </ul>
      </div>
      <div className="mt-4 flex flex-col items-start rounded-lg border border-gray-200 bg-white p-4 shadow-md">
        <h2 className="mb-4 text-2xl font-bold text-blue-700">Cena</h2>
        <div>
          <input
            type="text"
            name="minPrice"
            onChange={handlePriceChange}
            id="minPrice"
            placeholder="Od"
          />
          <input
            type="text"
            name="maxPrice"
            onChange={handlePriceChange}
            id="maxPrice"
            placeholder="Do"
          />
        </div>
      </div>
    </>
  );
};

export { FilterBar };
