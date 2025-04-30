import { H1 } from "@/core/components/ui/h1";
import { SortSelect } from "@/products/components";

export const ProductListPageHeader = () => {
  return (
    <header className="mb-1 mt-5 flex justify-between lg:mt-0">
      <H1 className="text-primary mb-0">Produkty</H1>
      <SortSelect />
    </header>
  );
};
