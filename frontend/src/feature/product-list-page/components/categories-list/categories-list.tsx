'use client';
import { useFetchCategoriesList } from "@/products/hooks/client/use-fetch-categories-list";

const CategoriesList = () => {
  const { data: categoriesList, isLoading, error } = useFetchCategoriesList();

  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Something went wrong!</div>;

  return (
    <div className="flex flex-col items-start bg-white text-blue-700 p-2 w-1/6 rounded-lg">
      <h2 className="font-bold text-2xl">Categories</h2>
      <ul>
        {categoriesList.map((category, index) => (
          <li key={index}>{category}</li>
        ))}
      </ul>
    </div>
  );
};

export { CategoriesList };
