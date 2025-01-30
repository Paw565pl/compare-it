interface ProductPageProps {
  readonly id: string;
}

export const ProductPage = ({ id }: ProductPageProps) => {
  return <>{id}</>;
};
