import Image from "next/image";
import Link from "next/link";

const SingleProduct = () => {
  return (
    <div className="w-2/3 rounded-lg border-2 border-blue-700 p-4 text-blue-700">
      <Image widht={200} height={200} />
      <div className="flex justify-center">
        <h1>Title</h1>
        <div>Gwiazdki</div>
        <Link href={"link"}>
          <div>Porównaj oferty</div>
        </Link>
      </div>
    </div>
  );
};

export { SingleProduct };
