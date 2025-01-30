import { ProductPageImage } from "@/products/components/index";
interface ProductPageImagesProps {
  readonly name: string;
  readonly images: string[];
}

export const ProductPageImages = ({ name, images }: ProductPageImagesProps) => {
  return (
    <div className="mt-4 flex flex-wrap justify-center gap-1">
      {images.map((imageUrl, imageUrlIndex) => (
        <ProductPageImage key={imageUrlIndex} name={name} imageUrl={imageUrl} />
      ))}
    </div>
  );
};
