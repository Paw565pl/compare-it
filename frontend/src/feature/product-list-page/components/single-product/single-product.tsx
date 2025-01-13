import { SingleProduct } from "@/feature/product-list-page/types/single-product";
import { Star } from "lucide-react";
import Image from "next/image";
import Link from "next/link";

const SingleProduct = ({
  category,
  ean,
  id,
  isAvailable,
  lowestCurrentPrice,
  lowestPriceShop,
  mainImageUrl,
  name,
  offerCount,
}: SingleProduct) => {
  return (
    <div className="flex rounded-lg border-2 border-blue-700 p-4 text-blue-700">
      <Image src={mainImageUrl} width={200} height={200} alt={name} />
      <div className="flex justify-center">
        <h1 className="mr-4">{name}</h1>
        <div className="flex text-white">
          <Star />
          <Star />
          <Star />
          <Star />
          <Star />
        </div>
        <Link href={"link"}>
          <div className="rounded-lg bg-blue-700 p-2 text-white">
            Porównaj oferty
          </div>
        </Link>
      </div>
    </div>
  );
};

export { SingleProduct };
