import pymysql, os, segno
from dotenv import load_dotenv
from flask import Flask, request, jsonify, send_file

load_dotenv()

conn = pymysql.connect(
    host=os.environ.get('dbhost'),
    user=os.environ.get('dbuser'),
    password=os.environ.get('dbpassword'),
    db=os.environ.get('dbname'),
    charset='utf8'
)

cur = conn.cursor()

app = Flask(__name__)

# checks if there's any missing parameters
def paramCheck(params: list[str], get: bool = True):
    paramValues = {}

    for param in params:
        paramValue = request.args.get(param) if get else request.form.get(param)

        if paramValue is None or paramValue == "":
            return jsonify({"msg": f"{param} is required"}), 400
        else:
            paramValues[param] = paramValue

    return paramValues

# registers the user
@app.route("/register", methods=["POST"])
def register():
    paramValues = paramCheck(["email", "password", "name", "phone", "avatar"], get=False)

    if type(paramValues) == tuple:
        return paramValues

    sql = "SELECT * FROM userInfo WHERE email=%s"
    cur.execute(sql, paramValues["email"])
    emailMatch = cur.fetchall()

    if len(emailMatch) != 0:
        return jsonify({"msg": "email already registered"}), 400

    sql = "INSERT INTO userInfo (email, password, name, phone, avatar) VALUES (%s, %s, %s, %s, %s)"
    cur.execute(sql, [paramValues["email"], paramValues["password"], paramValues["name"], paramValues["phone"], paramValues["avatar"]])
    conn.commit()

    sql = "SELECT * FROM userInfo WHERE email=%s"
    cur.execute(sql, paramValues["email"])
    emailMatch = cur.fetchall()

    userId = emailMatch[0][0]

    qrcode = segno.make_qr(userId)
    qrcode.save(f"qrcodes/qr_{userId}.png", scale=20)

    return jsonify({"userId": userId}), 200

# logins the user
@app.route("/login", methods=["POST"])
def login():
    paramValues = paramCheck(["email", "password"], get=False)

    if type(paramValues) == tuple:
        return paramValues

    sql = "SELECT * FROM userInfo WHERE email=%s"
    cur.execute(sql, paramValues["email"])
    emailMatch = cur.fetchall()

    if len(emailMatch) == 0:
        return jsonify({"msg": "invalid email"}), 400
    elif emailMatch[0][2] != paramValues["password"]:
        return jsonify({"msg": "invalid password"}), 400
    else:
        return jsonify({"userId": emailMatch[0][0]}), 200

# get info of users
@app.route("/getUsersInfo", methods=["GET"])
def getUsersInfo():
    paramValues = paramCheck(["userIds"])
    userIds = paramCheck(["userIds"])["userIds"].split(',')

    if userIds == ['-1']:
        return jsonify([]), 200

    if type(paramValues) == tuple:
        return paramValues

    toReturn = []

    for userId in userIds:
        userId = int(userId)
        sql = "SELECT * FROM userInfo WHERE userId=%s"
        cur.execute(sql, userId)
        result = cur.fetchall()

        if len(result) == 0:
            return jsonify({"msg": "invalid userId"}), 400

        result = result[0]

        toReturn.append({
            "email": result[1],
            "name": result[3],
            "bio": result[4] if result[4] is not None else "",
            "occupation": result[5] if result[5] is not None else "",
            "phone": result[6],
            "school": result[7],
            "company": result[8],
            "avatar": result[9]
        })

    return jsonify(toReturn), 200

# edit info of user
@app.route("/editUserInfo", methods=["PUT"])
def editUserInfo ():
    paramValues = paramCheck(["userId", "name", "bio", "occupation", "phone", "school", "company", "avatar"], get=False)

    if type(paramValues) == tuple:
        return paramValues

    sql = "SELECT * FROM userInfo WHERE userId=%s"
    cur.execute(sql, paramValues["userId"])
    result = cur.fetchall()

    if len(result) == 0:
        return jsonify({"msg": "invalid userId"}), 400

    sql = "UPDATE userInfo SET name=%s, bio=%s, occupation=%s, phone=%s, school=%s, company=%s, avatar=%s WHERE userId=%s"
    cur.execute(sql, [paramValues["name"], paramValues["bio"], paramValues["occupation"], paramValues["phone"],
                      paramValues["school"], paramValues["company"], paramValues["avatar"], paramValues["userId"]])
    conn.commit()

    return jsonify({"msg": "successfully edited"}), 200

# add card in wallet
@app.route("/addCard", methods=["POST"])
def addCard():
    paramValues = paramCheck(["userId", "cardUserId"])

    if type(paramValues) == tuple:
        return paramValues

    sql = "SELECT * FROM userInfo WHERE userId=%s"
    cur.execute(sql, paramValues["userId"])
    result = cur.fetchall()

    if len(result) == 0:
        return jsonify({"msg": "invalid userId"}), 400

    sql = "SELECT * FROM userInfo WHERE userId=%s"
    cur.execute(sql, paramValues["cardUserId"])
    result = cur.fetchall()

    if len(result) == 0:
        return jsonify({"msg": "invalid cardUserId"}), 400

    sql = "SELECT * FROM cardInfo WHERE userId=%s AND cardUserId=%s"
    cur.execute(sql, [paramValues["userId"], paramValues["cardUserId"]])
    result = cur.fetchall()

    if len(result) != 0:
        return jsonify({"msg": "already existing card"}), 400

    sql = "INSERT INTO cardInfo (userId, cardUserId) VALUES (%s, %s)"
    cur.execute(sql, [paramValues["userId"], paramValues["cardUserId"]])
    conn.commit()

    return jsonify({"msg": "successfully added card"}), 200

# delete card in wallet
@app.route("/delCard", methods=["DELETE"])
def delCard():
    paramValues = paramCheck(["userId", "cardUserId"])

    if type(paramValues) == tuple:
        return paramValues

    sql = "SELECT * FROM userInfo WHERE userId=%s"
    cur.execute(sql, paramValues["userId"])
    result = cur.fetchall()

    if len(result) == 0:
        return jsonify({"msg": "invalid userId"}), 400

    sql = "SELECT * FROM userInfo WHERE userId=%s"
    cur.execute(sql, paramValues["cardUserId"])
    result = cur.fetchall()

    if len(result) == 0:
        return jsonify({"msg": "invalid cardUserId"}), 400

    sql = "SELECT * FROM cardInfo WHERE userId=%s AND cardUserId=%s"
    cur.execute(sql, [paramValues["userId"], paramValues["cardUserId"]])
    result = cur.fetchall()

    if len(result) == 0:
        return jsonify({"msg": "card doesn't exist"}), 400

    sql = "DELETE FROM cardInfo WHERE userId=%s AND cardUserId=%s"
    cur.execute(sql, [paramValues["userId"], paramValues["cardUserId"]])
    conn.commit()

    return jsonify({"msg": "successfully deleted card"}), 200

# get cards of user
@app.route("/getCards", methods=["GET"])
def getCards():
    paramValues = paramCheck(["userId"])

    if type(paramValues) == tuple:
        return paramValues

    cards = []

    sql = "SELECT * FROM userInfo WHERE userId=%s"
    cur.execute(sql, paramValues["userId"])
    result = cur.fetchall()

    if len(result) == 0:
        return jsonify({"msg": "invalid userId"}), 400

    sql = "SELECT * FROM cardInfo WHERE userId=%s"
    cur.execute(sql, paramValues["userId"])
    result = cur.fetchall()

    for card in result:
        cards.append(card[2])

    return jsonify({"cardUserIds": cards}), 200

# get qr code of user's userId
@app.route("/getQr", methods=["GET"])
def getQr():
    paramValues = paramCheck(["userId"])

    if type(paramValues) == tuple:
        return paramValues

    sql = "SELECT * FROM userInfo WHERE userId=%s"
    cur.execute(sql, paramValues["userId"])
    result = cur.fetchall()

    if len(result) == 0:
        return jsonify({"msg": "invalid userId"}), 400

    return send_file(f"qrcodes/qr_{paramValues['userId']}.png")

if __name__ == "__main__":
    app.run(debug=True, port=4999)

cur.close()
conn.close()