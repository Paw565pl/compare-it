import {
  ProductPageComments,
  ProductPageOffers,
  ProductPageTop,
} from "@/products/components/index";

export interface ProductPageProps {
  readonly id: string;
}

export const ProductPage = ({ id }: ProductPageProps) => {
  return (
    <div className="flex flex-col">
      <ProductPageTop id={id as string} />
      <ProductPageOffers id={id as string} />
      <ProductPageComments />
    </div>
  );
};
