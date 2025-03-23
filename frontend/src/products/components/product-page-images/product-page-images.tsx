import { ImageWithFallback } from "@/core/components/ui/image-with-fallback";

interface ProductPageImagesProps {
  readonly name: string;
  readonly images: string[];
}

export const ProductPageImages = ({ name, images }: ProductPageImagesProps) => {
  return (
    <div className="mt-4 flex flex-wrap justify-center gap-1">
      {images.map((imageUrl, imageUrlIndex) => (
        <ImageWithFallback
          key={imageUrlIndex}
          name={name}
          imageUrl={imageUrl}
          width={200}
          height={200}
        />
      ))}
    </div>
  );
};
